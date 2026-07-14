package com.justeat.service;

import com.justeat.dto.OrderDTO;
import com.justeat.dto.OrderItemDTO;
import com.justeat.dto.PlaceOrderRequest;
import com.justeat.entity.*;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.exception.UnauthorizedException;
import com.justeat.repository.*;
import com.justeat.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

        private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository; // +++ needed for reorder +++
        private final RestaurantRepository restaurantRepository;
        private final MenuItemRepository menuItemRepository;
        private final MenuItemRatingRepository menuItemRatingRepository;
        private final UserRepository userRepository;
        private final SecurityUtil securityUtil;
        private final MenuItemService menuItemService;

        @Transactional
        public OrderDTO placeOrder(PlaceOrderRequest request) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Placing order for user: {}", username);

                User customer = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (customer.getRole() != Role.CUSTOMER) {
                        throw new UnauthorizedException("Only customers can place orders");
                }

                Cart cart = cartRepository.findByUser(customer)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

                if (cart.getItems().isEmpty()) {
                        throw new RuntimeException("Cart is empty");
                }

                Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

                boolean allItemsFromRestaurant = cart.getItems().stream()
                                .allMatch(item -> item.getMenuItem().getRestaurant().getId()
                                                .equals(restaurant.getId()));

                if (!allItemsFromRestaurant) {
                        throw new RuntimeException("All items must be from the same restaurant");
                }

                Order order = Order.builder()
                                .customer(customer)
                                .restaurant(restaurant)
                                .items(new ArrayList<>())
                                .totalPrice(cart.getTotalPrice())
                                .status(OrderStatus.PENDING)
                                .deliveryAddress(request.getDeliveryAddress()) // +++
                                .build();

                order = orderRepository.save(order);

                for (CartItem cartItem : cart.getItems()) {
                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .menuItem(cartItem.getMenuItem())
                                        .quantity(cartItem.getQuantity())
                                        .price(cartItem.getMenuItem().getPrice())
                                        .build();
                        order.getItems().add(orderItem);
                        orderItemRepository.save(orderItem);

                        MenuItem menuItem = cartItem.getMenuItem();
                        menuItem.setOrderCount(menuItem.getOrderCount() + cartItem.getQuantity());
                        menuItemRepository.save(menuItem);
                }

                cart.getItems().clear();
                cartRepository.save(cart);

                menuItemService.updateMostOrderedItems(restaurant.getId());

                logger.info("Order placed successfully: Order ID {}", order.getId());

                return convertToDTO(order);
        }

        // +++ Extended: supports optional filter by restaurantName and status +++
        public List<OrderDTO> getOrderHistory(String restaurantName, String status) {
                String username = securityUtil.getCurrentUsername();
                logger.debug("Fetching order history for user: {}", username);

                User customer = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return orderRepository.findByCustomerOrderByOrderDateDesc(customer).stream()
                                .filter(order -> restaurantName == null || restaurantName.isBlank() ||
                                                order.getRestaurant().getName().toLowerCase()
                                                                .contains(restaurantName.toLowerCase()))
                                .filter(order -> status == null || status.isBlank() ||
                                                order.getStatus().name().equalsIgnoreCase(status))
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        public OrderDTO getOrderStatus(Long orderId) {
                String username = securityUtil.getCurrentUsername();
                logger.debug("Fetching order status for order ID: {}", orderId);

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!order.getCustomer().getId().equals(currentUser.getId()) &&
                                !order.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("Unauthorized to view this order");
                }

                return convertToDTO(order);
        }

        public List<OrderDTO> getRestaurantOrders() {
                String username = securityUtil.getCurrentUsername();
                logger.debug("Fetching restaurant orders for owner: {}", username);

                User owner = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (owner.getRole() != Role.RESTAURANT_OWNER) {
                        throw new UnauthorizedException("Only restaurant owners can view orders");
                }

                List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);

                return restaurants.stream()
                                .flatMap(restaurant -> orderRepository.findByRestaurantOrderByOrderDateDesc(restaurant)
                                                .stream())
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Transactional
        public OrderDTO updateOrderStatus(Long orderId, String status) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Updating order status for order ID: {} by user: {}", orderId, username);

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!order.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("Only restaurant owners can update order status");
                }

                try {
                        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
                        order.setStatus(newStatus);
                        orderRepository.save(order);
                        logger.info("Order status updated successfully to: {}", newStatus);

                        // +++ Recalculate popularity when an order reaches COMPLETED +++
                        if (newStatus == OrderStatus.COMPLETED) {
                                menuItemService.updateMostOrderedItems(order.getRestaurant().getId());
                        }
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid order status: " + status);
                }

                return convertToDTO(order);
        }

        // +++ Reorder: clears current cart and re-adds all available items from a past
        // order +++
        @Transactional
        public void reorder(Long orderId) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Reorder from order ID: {} for user: {}", orderId, username);

                User customer = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                if (!order.getCustomer().getId().equals(customer.getId())) {
                        throw new UnauthorizedException("Unauthorized to reorder this order");
                }

                // Get or create cart
                Cart cart = cartRepository.findByUser(customer).orElseGet(() -> {
                        Cart newCart = Cart.builder().user(customer).build();
                        return cartRepository.save(newCart);
                });

                // Clear existing cart items
                cart.getItems().clear();
                cartRepository.save(cart);

                // Re-add items from previous order (skip unavailable ones)
                for (OrderItem orderItem : order.getItems()) {
                        MenuItem menuItem = orderItem.getMenuItem();
                        if (!menuItem.getAvailable()) {
                                logger.warn("Skipping unavailable item during reorder: {}", menuItem.getName());
                                continue;
                        }
                        CartItem cartItem = CartItem.builder()
                                        .cart(cart)
                                        .menuItem(menuItem)
                                        .quantity(orderItem.getQuantity())
                                        .build();
                        cart.getItems().add(cartItem);
                        cartItemRepository.save(cartItem);
                }

                cartRepository.save(cart);
                logger.info("Reorder complete: {} item(s) added to cart for order ID: {}",
                                cart.getItems().size(), orderId);
        }

        private OrderDTO convertToDTO(Order order) {
                List<OrderItemDTO> items = order.getItems().stream()
                                .map(item -> OrderItemDTO.builder()
                                                .id(item.getId())
                                                .menuItemId(item.getMenuItem().getId())
                                                .menuItemName(item.getMenuItem().getName())
                                                .quantity(item.getQuantity())
                                                .price(item.getPrice())
                                                .customerRating(menuItemRatingRepository
                                                                .findByCustomerAndOrderAndMenuItem(order.getCustomer(),
                                                                                order,
                                                                                item.getMenuItem())
                                                                .map(MenuItemRating::getRating)
                                                                .orElse(null))
                                                .build())
                                .collect(Collectors.toList());

                return OrderDTO.builder()
                                .id(order.getId())
                                .customerId(order.getCustomer().getId())
                                .customerName(order.getCustomer().getFullName())
                                .restaurantId(order.getRestaurant().getId())
                                .restaurantName(order.getRestaurant().getName())
                                .items(items)
                                .totalPrice(order.getTotalPrice())
                                .status(order.getStatus().name())
                                .orderDate(order.getOrderDate())
                                .deliveryAddress(order.getDeliveryAddress()) // +++
                                .build();
        }
}
