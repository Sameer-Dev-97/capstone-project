package com.justeat.service;

import com.justeat.dto.OrderDTO;
import com.justeat.dto.PlaceOrderRequest;
import com.justeat.entity.*;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.exception.UnauthorizedException;
import com.justeat.repository.*;
import com.justeat.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private OrderItemRepository orderItemRepository;

        @Mock
        private CartRepository cartRepository;

        @Mock
        private RestaurantRepository restaurantRepository;

        @Mock
        private MenuItemRepository menuItemRepository;

        @Mock
        private MenuItemRatingRepository menuItemRatingRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SecurityUtil securityUtil;

        @Mock
        private MenuItemService menuItemService;

        @InjectMocks
        private OrderService orderService;

        private User customer;
        private User owner;
        private Restaurant restaurant;
        private MenuItem menuItem;
        private Cart cart;
        private CartItem cartItem;
        private Order order;

        @BeforeEach
        void setUp() {
                customer = User.builder()
                                .id(1L)
                                .username("customer")
                                .fullName("Customer Name")
                                .role(Role.CUSTOMER)
                                .build();

                owner = User.builder()
                                .id(2L)
                                .username("owner")
                                .role(Role.RESTAURANT_OWNER)
                                .build();

                restaurant = Restaurant.builder()
                                .id(1L)
                                .name("Test Restaurant")
                                .owner(owner)
                                .build();

                menuItem = MenuItem.builder()
                                .id(1L)
                                .name("Test Item")
                                .price(10.0)
                                .available(true)
                                .orderCount(0)
                                .restaurant(restaurant)
                                .build();

                cartItem = CartItem.builder()
                                .id(1L)
                                .menuItem(menuItem)
                                .quantity(2)
                                .build();

                cart = Cart.builder()
                                .id(1L)
                                .user(customer)
                                .items(new ArrayList<>(List.of(cartItem)))
                                .build();

                cartItem.setCart(cart);

                order = Order.builder()
                                .id(1L)
                                .customer(customer)
                                .restaurant(restaurant)
                                .totalPrice(20.0)
                                .status(OrderStatus.PENDING)
                                .items(new ArrayList<>())
                                .build();
        }

        @Test
        void testPlaceOrder_Success() {
                PlaceOrderRequest request = new PlaceOrderRequest();
                request.setRestaurantId(1L);

                when(securityUtil.getCurrentUsername()).thenReturn("customer");
                when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
                when(cartRepository.findByUser(customer)).thenReturn(Optional.of(cart));
                when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
                when(orderRepository.save(any(Order.class))).thenReturn(order);
                when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
                when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);
                when(cartRepository.save(any(Cart.class))).thenReturn(cart);
                when(menuItemRatingRepository.findByCustomerAndOrderAndMenuItem(customer, order, menuItem))
                                .thenReturn(Optional.empty());

                OrderDTO result = orderService.placeOrder(request);

                assertNotNull(result);
                assertEquals(1L, result.getId());
                verify(orderRepository, times(1)).save(any(Order.class));
                verify(cartRepository, times(1)).save(any(Cart.class));
        }

        @Test
        void testPlaceOrder_EmptyCart() {
                cart.getItems().clear();
                PlaceOrderRequest request = new PlaceOrderRequest();
                request.setRestaurantId(1L);

                when(securityUtil.getCurrentUsername()).thenReturn("customer");
                when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
                when(cartRepository.findByUser(customer)).thenReturn(Optional.of(cart));

                assertThrows(RuntimeException.class, () -> orderService.placeOrder(request));
                verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void testPlaceOrder_NotCustomer() {
                owner.setRole(Role.RESTAURANT_OWNER);
                PlaceOrderRequest request = new PlaceOrderRequest();
                request.setRestaurantId(1L);

                when(securityUtil.getCurrentUsername()).thenReturn("owner");
                when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

                assertThrows(UnauthorizedException.class, () -> orderService.placeOrder(request));
                verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void testGetOrderHistory_ReturnsPreviouslyRatedItem() {
                OrderItem orderItem = OrderItem.builder()
                                .id(1L)
                                .order(order)
                                .menuItem(menuItem)
                                .quantity(1)
                                .price(10.0)
                                .build();
                order.setItems(new ArrayList<>(List.of(orderItem)));

                MenuItemRating rating = MenuItemRating.builder()
                                .id(1L)
                                .customer(customer)
                                .menuItem(menuItem)
                                .rating(4.0)
                                .build();

                when(securityUtil.getCurrentUsername()).thenReturn("customer");
                when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
                when(orderRepository.findByCustomerOrderByOrderDateDesc(customer)).thenReturn(List.of(order));
                when(menuItemRatingRepository.findByCustomerAndOrderAndMenuItem(customer, order, menuItem))
                                .thenReturn(Optional.of(rating));

                OrderDTO result = orderService.getOrderHistory("", "").get(0);

                assertNotNull(result.getItems());
                assertEquals(4.0, result.getItems().get(0).getCustomerRating());
        }
}
