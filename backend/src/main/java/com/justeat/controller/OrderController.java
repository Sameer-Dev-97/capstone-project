package com.justeat.controller;

import com.justeat.dto.OrderDTO;
import com.justeat.dto.PlaceOrderRequest;
import com.justeat.dto.UpdateOrderStatusRequest;
import com.justeat.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place an order (Customer only)")
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get order history (Customer only)")
    public ResponseEntity<List<OrderDTO>> getOrderHistory(
            @RequestParam(required = false) String restaurantName,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getOrderHistory(restaurantName, status));
    }

    // +++ Reorder: clears cart and adds items from a past order +++
    @PostMapping("/{id}/reorder")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Reorder a previous order (Customer only)")
    public ResponseEntity<Void> reorder(@PathVariable Long id) {
        orderService.reorder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get order status")
    public ResponseEntity<OrderDTO> getOrderStatus(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderStatus(id));
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get restaurant orders (Owner only)")
    public ResponseEntity<List<OrderDTO>> getRestaurantOrders() {
        return ResponseEntity.ok(orderService.getRestaurantOrders());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update order status (Owner only)")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }
}
