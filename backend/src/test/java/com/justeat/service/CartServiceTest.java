package com.justeat.service;

import com.justeat.dto.CartDTO;
import com.justeat.dto.CartItemRequest;
import com.justeat.entity.*;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.repository.*;
import com.justeat.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private MenuItem menuItem;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("customer")
                .role(Role.CUSTOMER)
                .build();

        User owner = User.builder()
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
                .restaurant(restaurant)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void testGetCart_ExistingCart() {
        when(securityUtil.getCurrentUsername()).thenReturn("customer");
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartDTO result = cartService.getCart();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepository, times(1)).findByUser(user);
    }

    @Test
    void testAddToCart_Success() {
        CartItemRequest request = new CartItemRequest();
        request.setMenuItemId(1L);
        request.setQuantity(2);

        when(securityUtil.getCurrentUsername()).thenReturn("customer");
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.addToCart(request);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_ItemNotAvailable() {
        menuItem.setAvailable(false);
        CartItemRequest request = new CartItemRequest();
        request.setMenuItemId(1L);
        request.setQuantity(2);

        when(securityUtil.getCurrentUsername()).thenReturn("customer");
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        assertThrows(RuntimeException.class, () -> cartService.addToCart(request));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }
}
