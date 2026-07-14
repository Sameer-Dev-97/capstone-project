package com.justeat.controller;

import com.justeat.dto.MenuItemDTO;
import com.justeat.dto.MenuItemRequest;
import com.justeat.dto.MenuItemRatingRequest;
import com.justeat.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Menu Items", description = "Menu item management APIs")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/restaurants/{restaurantId}/menu")
    @Operation(summary = "Get restaurant menu")
    public ResponseEntity<List<MenuItemDTO>> getRestaurantMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.getMenuByRestaurant(restaurantId));
    }

    // +++ Returns only items flagged as mostOrdered (popular) for a restaurant +++
    @GetMapping("/restaurants/{restaurantId}/popular-items")
    @Operation(summary = "Get popular menu items for a restaurant")
    public ResponseEntity<List<MenuItemDTO>> getPopularItems(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.getPopularItems(restaurantId));
    }

    // +++ Manually recalculates popularity flags (Owner only) +++
    @PostMapping("/admin/restaurants/{restaurantId}/refresh-popularity")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Manually refresh popularity flags (Owner only)")
    public ResponseEntity<Void> refreshPopularity(@PathVariable Long restaurantId) {
        menuItemService.refreshPopularity(restaurantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create menu item (Owner only)")
    public ResponseEntity<MenuItemDTO> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.createMenuItem(restaurantId, request));
    }

    @PutMapping("/menu/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update menu item (Owner only)")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, request));
    }

    @DeleteMapping("/menu/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete menu item (Owner only)")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/menu/{id}/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Rate a menu item (Customer only, must have completed order with this item)")
    public ResponseEntity<MenuItemDTO> rateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRatingRequest request) {
        return ResponseEntity.ok(menuItemService.rateMenuItem(id, request.getOrderId(), request.getRating()));
    }
}
