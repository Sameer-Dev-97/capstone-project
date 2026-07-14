package com.justeat.service;

import com.justeat.dto.CartDTO;
import com.justeat.dto.CartItemDTO;
import com.justeat.dto.CartItemRequest;
import com.justeat.dto.MenuItemDTO;
import com.justeat.entity.*;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.repository.*;
import com.justeat.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public CartDTO getCart() {
        String username = securityUtil.getCurrentUsername();
        logger.debug("Fetching cart for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        return convertToDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(CartItemRequest request) {
        String username = securityUtil.getCurrentUsername();
        logger.info("Adding item to cart for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getAvailable()) {
            throw new RuntimeException("Menu item is not available");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(request.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        logger.info("Item added to cart successfully");

        return convertToDTO(cart);
    }

    @Transactional
    public CartDTO updateCartItem(Long cartItemId, CartItemRequest request) {
        String username = securityUtil.getCurrentUsername();
        logger.info("Updating cart item ID: {} for user: {}", cartItemId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        logger.info("Cart item updated successfully");

        return convertToDTO(cartItem.getCart());
    }

    @Transactional
    public CartDTO removeFromCart(Long cartItemId) {
        String username = securityUtil.getCurrentUsername();
        logger.info("Removing cart item ID: {} for user: {}", cartItemId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Cart cart = cartItem.getCart();
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        logger.info("Cart item removed successfully");

        return convertToDTO(cart);
    }

    @Transactional
    public void clearCart() {
        String username = securityUtil.getCurrentUsername();
        logger.info("Clearing cart for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);

        logger.info("Cart cleared successfully");
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(this::convertCartItemToDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    private CartItemDTO convertCartItemToDTO(CartItem cartItem) {
        MenuItem menuItem = cartItem.getMenuItem();
        MenuItemDTO menuItemDTO = MenuItemDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .restaurantId(menuItem.getRestaurant().getId())
                .build();

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .menuItem(menuItemDTO)
                .quantity(cartItem.getQuantity())
                .subtotal(menuItem.getPrice() * cartItem.getQuantity())
                .build();
    }
}
