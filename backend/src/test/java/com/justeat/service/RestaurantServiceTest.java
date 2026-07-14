package com.justeat.service;

import com.justeat.dto.RestaurantDTO;
import com.justeat.dto.RestaurantRequest;
import com.justeat.entity.Restaurant;
import com.justeat.entity.Role;
import com.justeat.entity.User;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.exception.UnauthorizedException;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import com.justeat.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private RestaurantService restaurantService;

    private User owner;
    private Restaurant restaurant;
    private RestaurantRequest request;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .username("owner")
                .role(Role.RESTAURANT_OWNER)
                .build();

        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .location("Test Location")
                .cuisine("Italian")
                .rating(4.5)
                .owner(owner)
                .build();

        request = new RestaurantRequest();
        request.setName("Test Restaurant");
        request.setLocation("Test Location");
        request.setCuisine("Italian");
    }

    @Test
    void testGetAllRestaurants() {
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(restaurant));

        List<RestaurantDTO> result = restaurantService.getAllRestaurants();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Restaurant", result.get(0).getName());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testGetRestaurantById_Success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        RestaurantDTO result = restaurantService.getRestaurantById(1L);

        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRestaurantById_NotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getRestaurantById(1L));
    }

    @Test
    void testCreateRestaurant_Success() {
        when(securityUtil.getCurrentUsername()).thenReturn("owner");
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        RestaurantDTO result = restaurantService.createRestaurant(request);

        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testCreateRestaurant_NotOwner() {
        User customer = User.builder()
                .id(2L)
                .username("customer")
                .role(Role.CUSTOMER)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("customer");
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class, () -> restaurantService.createRestaurant(request));
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }
}
