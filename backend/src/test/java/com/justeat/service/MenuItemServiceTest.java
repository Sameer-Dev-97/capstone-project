package com.justeat.service;

import com.justeat.dto.MenuItemDTO;
import com.justeat.entity.MenuItem;
import com.justeat.entity.MenuItemRating;
import com.justeat.entity.Order;
import com.justeat.entity.OrderItem;
import com.justeat.entity.OrderStatus;
import com.justeat.entity.Restaurant;
import com.justeat.entity.Role;
import com.justeat.entity.User;
import com.justeat.repository.CartItemRepository;
import com.justeat.repository.MenuItemRatingRepository;
import com.justeat.repository.MenuItemRepository;
import com.justeat.repository.OrderItemRepository;
import com.justeat.repository.OrderRepository;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import com.justeat.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

        @Mock
        private MenuItemRepository menuItemRepository;

        @Mock
        private MenuItemRatingRepository menuItemRatingRepository;

        @Mock
        private OrderItemRepository orderItemRepository;

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private CartItemRepository cartItemRepository;

        @Mock
        private RestaurantRepository restaurantRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SecurityUtil securityUtil;

        @InjectMocks
        private MenuItemService menuItemService;

        private User customer;
        private Restaurant restaurant;
        private MenuItem menuItem;
        private Order completedOrder;

        @BeforeEach
        void setUp() {
                customer = User.builder()
                                .id(1L)
                                .username("customer")
                                .role(Role.CUSTOMER)
                                .build();

                restaurant = Restaurant.builder()
                                .id(1L)
                                .name("Test Restaurant")
                                .location("Test Location")
                                .cuisine("Italian")
                                .rating(4.0)
                                .owner(customer)
                                .build();

                menuItem = MenuItem.builder()
                                .id(1L)
                                .name("Test Item")
                                .description("Test Description")
                                .price(10.0)
                                .category("Main")
                                .available(true)
                                .todaysSpecial(false)
                                .dealOfDay(false)
                                .mostOrdered(false)
                                .orderCount(0)
                                .rating(0.0)
                                .restaurant(restaurant)
                                .build();

                completedOrder = Order.builder()
                                .id(10L)
                                .customer(customer)
                                .restaurant(restaurant)
                                .status(OrderStatus.COMPLETED)
                                .items(new java.util.ArrayList<>())
                                .build();

                OrderItem orderItem = OrderItem.builder()
                                .id(100L)
                                .order(completedOrder)
                                .menuItem(menuItem)
                                .quantity(1)
                                .price(menuItem.getPrice())
                                .build();
                completedOrder.getItems().add(orderItem);
        }

        @Test
        void rateMenuItem_AllowsFirstRating() {
                MenuItemRating savedRating = MenuItemRating.builder()
                                .id(1L)
                                .customer(customer)
                                .menuItem(menuItem)
                                .rating(5.0)
                                .build();

                when(securityUtil.getCurrentUsername()).thenReturn("customer");
                when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
                when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
                when(orderRepository.findById(10L)).thenReturn(Optional.of(completedOrder));
                when(menuItemRatingRepository.existsByCustomerAndOrderAndMenuItem(customer, completedOrder, menuItem))
                                .thenReturn(false);
                when(menuItemRatingRepository.save(any(MenuItemRating.class))).thenReturn(savedRating);
                when(menuItemRatingRepository.findAverageRatingByMenuItemId(1L)).thenReturn(Optional.of(5.0));
                when(menuItemRatingRepository.findAverageRatingByRestaurantId(1L)).thenReturn(Optional.of(5.0));
                when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);
                when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

                MenuItemDTO result = menuItemService.rateMenuItem(1L, 10L, 5.0);

                assertEquals(1L, result.getId());
                verify(menuItemRatingRepository).save(any(MenuItemRating.class));
        }

        @Test
        void rateMenuItem_RejectsSecondRating() {
                when(securityUtil.getCurrentUsername()).thenReturn("customer");
                when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
                when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
                when(orderRepository.findById(10L)).thenReturn(Optional.of(completedOrder));
                when(menuItemRatingRepository.existsByCustomerAndOrderAndMenuItem(customer, completedOrder, menuItem))
                                .thenReturn(true);

                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> menuItemService.rateMenuItem(1L, 10L, 4.0));

                assertEquals("You have already rated this item for this order", exception.getMessage());
                verify(menuItemRatingRepository, never()).save(any(MenuItemRating.class));
                verify(menuItemRepository, never()).save(any(MenuItem.class));
                verify(restaurantRepository, never()).save(any(Restaurant.class));
        }
}