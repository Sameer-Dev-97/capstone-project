package com.justeat.service;

import com.justeat.dto.MenuItemDTO;
import com.justeat.dto.MenuItemRequest;
import com.justeat.entity.MenuItem;
import com.justeat.entity.MenuItemRating;
import com.justeat.entity.OrderStatus;
import com.justeat.entity.Restaurant;
import com.justeat.entity.Role;
import com.justeat.entity.User;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.exception.UnauthorizedException;
import com.justeat.repository.MenuItemRatingRepository;
import com.justeat.repository.MenuItemRepository;
import com.justeat.repository.OrderItemRepository;
import com.justeat.repository.OrderRepository;
import com.justeat.repository.CartItemRepository;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import com.justeat.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

        private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);

        private final MenuItemRepository menuItemRepository;
        private final MenuItemRatingRepository menuItemRatingRepository;
        private final OrderItemRepository orderItemRepository;
        private final OrderRepository orderRepository;
        private final CartItemRepository cartItemRepository;
        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;
        private final SecurityUtil securityUtil;

        // +++ Configurable popularity thresholds (application.properties) +++
        @Value("${menu.popularity.top-n:5}")
        private int popularityTopN;

        @Value("${menu.popularity.min-order-count:20}")
        private int popularityMinOrderCount;

        public List<MenuItemDTO> getMenuByRestaurant(Long restaurantId) {
                logger.debug("Fetching menu for restaurant ID: {}", restaurantId);

                Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

                return menuItemRepository.findByRestaurantAndAvailableTrue(restaurant).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Transactional
        public MenuItemDTO createMenuItem(Long restaurantId, MenuItemRequest request) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Creating menu item for restaurant ID: {} by user: {}", restaurantId, username);

                Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (currentUser.getRole() != Role.RESTAURANT_OWNER) {
                        throw new UnauthorizedException("Only restaurant owners can create menu items");
                }

                if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("You can only create menu items for your own restaurants");
                }

                MenuItem menuItem = MenuItem.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .price(request.getPrice())
                                .category(request.getCategory())
                                .available(request.getAvailable() != null ? request.getAvailable() : true)
                                .todaysSpecial(request.getTodaysSpecial() != null ? request.getTodaysSpecial() : false)
                                .dealOfDay(request.getDealOfDay() != null ? request.getDealOfDay() : false)
                                .restaurant(restaurant)
                                .build();

                menuItem = menuItemRepository.save(menuItem);
                logger.info("Menu item created successfully: {}", menuItem.getName());

                return convertToDTO(menuItem);
        }

        @Transactional
        public MenuItemDTO updateMenuItem(Long id, MenuItemRequest request) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Updating menu item ID: {} by user: {}", id, username);

                MenuItem menuItem = menuItemRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!menuItem.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("You can only update your own menu items");
                }

                menuItem.setName(request.getName());
                menuItem.setDescription(request.getDescription());
                menuItem.setPrice(request.getPrice());
                menuItem.setCategory(request.getCategory());
                if (request.getAvailable() != null)
                        menuItem.setAvailable(request.getAvailable());
                if (request.getTodaysSpecial() != null)
                        menuItem.setTodaysSpecial(request.getTodaysSpecial());
                if (request.getDealOfDay() != null)
                        menuItem.setDealOfDay(request.getDealOfDay());

                menuItem = menuItemRepository.save(menuItem);
                logger.info("Menu item updated successfully: {}", menuItem.getName());

                return convertToDTO(menuItem);
        }

        @Transactional
        public void deleteMenuItem(Long id) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Deleting menu item ID: {} by user: {}", id, username);

                MenuItem menuItem = menuItemRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!menuItem.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("You can only delete your own menu items");
                }

                // Block deletion if the item has been part of any order (preserves order
                // history)
                if (orderItemRepository.existsByMenuItem(menuItem)) {
                        throw new RuntimeException(
                                        "Cannot delete '" + menuItem.getName() + "' because it has been ordered. " +
                                                        "Mark it as unavailable instead.");
                }

                // Remove the item from any active carts before deleting
                cartItemRepository.deleteByMenuItem(menuItem);

                menuItemRepository.delete(menuItem);
                logger.info("Menu item deleted successfully");
        }

        // Returns only popular (mostOrdered=true) items for a restaurant
        public List<MenuItemDTO> getPopularItems(Long restaurantId) {
                logger.debug("Fetching popular items for restaurant ID: {}", restaurantId);

                Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

                return menuItemRepository.findByRestaurantAndMostOrderedTrue(restaurant).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        // Public entry-point for the manual refresh endpoint
        @Transactional
        public void refreshPopularity(Long restaurantId) {
                logger.info("Manual popularity refresh triggered for restaurant ID: {}", restaurantId);
                updateMostOrderedItems(restaurantId);
        }

        // +++ Updated: now uses completed orders only + configurable thresholds +++
        @Transactional
        public void updateMostOrderedItems(Long restaurantId) {
                logger.debug("Recalculating popularity for restaurant ID: {} (topN={}, minCount={})",
                                restaurantId, popularityTopN, popularityMinOrderCount);

                // 1. Aggregate total quantities from COMPLETED orders only
                List<Object[]> rows = orderItemRepository.sumQuantityByMenuItemAndStatus(
                                restaurantId, OrderStatus.COMPLETED);

                // menuItemId -> completedOrderQuantity
                Map<Long, Integer> completedCountMap = new HashMap<>();
                for (Object[] row : rows) {
                        Long menuItemId = (Long) row[0];
                        Integer qty = ((Number) row[1]).intValue();
                        completedCountMap.put(menuItemId, qty);
                }

                // 2. Determine top-N popular item IDs by completed order count
                Set<Long> topNIds = completedCountMap.entrySet().stream()
                                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
                                .limit(popularityTopN)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toSet());

                // 3. Load all menu items for this restaurant and update flags
                List<MenuItem> allItems = menuItemRepository.findByRestaurant(
                                restaurantRepository.findById(restaurantId)
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Restaurant not found")));

                for (MenuItem item : allItems) {
                        int completedCount = completedCountMap.getOrDefault(item.getId(), 0);

                        // +++ Update orderCount to reflect only completed orders +++
                        item.setOrderCount(completedCount);

                        // Popular = in top-N by completed orders AND meets minimum threshold
                        boolean popular = topNIds.contains(item.getId())
                                        && completedCount >= popularityMinOrderCount;

                        if (item.getMostOrdered() != popular) {
                                item.setMostOrdered(popular);
                        }
                        menuItemRepository.save(item);
                }

                logger.info("Popularity recalculation complete for restaurant ID: {}", restaurantId);
        }

        @Transactional
        public MenuItemDTO rateMenuItem(Long menuItemId, Long orderId, Double rating) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Rating menu item ID: {} by user: {}", menuItemId, username);

                User customer = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (customer.getRole() != Role.CUSTOMER) {
                        throw new UnauthorizedException("Only customers can rate menu items");
                }

                MenuItem menuItem = menuItemRepository.findById(menuItemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

                com.justeat.entity.Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                if (!order.getCustomer().getId().equals(customer.getId())) {
                        throw new UnauthorizedException("You can only rate your own completed orders");
                }

                if (order.getStatus() != OrderStatus.COMPLETED) {
                        throw new UnauthorizedException("You can only rate items from completed orders");
                }

                boolean hasOrdered = order.getItems().stream()
                                .anyMatch(orderItem -> orderItem.getMenuItem().getId().equals(menuItemId));

                if (!hasOrdered) {
                        throw new UnauthorizedException("This item does not belong to the specified completed order");
                }

                if (menuItemRatingRepository.existsByCustomerAndOrderAndMenuItem(customer, order, menuItem)) {
                        throw new RuntimeException("You have already rated this item for this order");
                }

                MenuItemRating menuItemRating = MenuItemRating.builder()
                                .customer(customer)
                                .order(order)
                                .menuItem(menuItem)
                                .build();
                menuItemRating.setRating(rating);
                menuItemRatingRepository.save(menuItemRating);

                // Recalculate and persist the average rating for this menu item
                Double avgItemRating = menuItemRatingRepository
                                .findAverageRatingByMenuItemId(menuItemId)
                                .orElse(0.0);
                menuItem.setRating(Math.round(avgItemRating * 10.0) / 10.0);
                menuItemRepository.save(menuItem);

                // Recalculate and persist the restaurant rating as average of all dish ratings
                Long restaurantId = menuItem.getRestaurant().getId();
                Double avgRestaurantRating = menuItemRatingRepository
                                .findAverageRatingByRestaurantId(restaurantId)
                                .orElse(0.0);
                Restaurant restaurant = menuItem.getRestaurant();
                restaurant.setRating(Math.round(avgRestaurantRating * 10.0) / 10.0);
                restaurantRepository.save(restaurant);

                logger.info("Rated menu item '{}' with {}, restaurant rating updated to {}",
                                menuItem.getName(), rating, restaurant.getRating());

                return convertToDTO(menuItem);
        }

        private MenuItemDTO convertToDTO(MenuItem menuItem) {
                return MenuItemDTO.builder()
                                .id(menuItem.getId())
                                .name(menuItem.getName())
                                .description(menuItem.getDescription())
                                .price(menuItem.getPrice())
                                .category(menuItem.getCategory())
                                .available(menuItem.getAvailable())
                                .todaysSpecial(menuItem.getTodaysSpecial())
                                .dealOfDay(menuItem.getDealOfDay())
                                .mostOrdered(menuItem.getMostOrdered())
                                .orderCount(menuItem.getOrderCount())
                                .rating(menuItem.getRating())
                                .restaurantId(menuItem.getRestaurant().getId())
                                .build();
        }
}
