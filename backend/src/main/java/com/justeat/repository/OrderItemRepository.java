package com.justeat.repository;

import com.justeat.entity.MenuItem;
import com.justeat.entity.OrderItem;
import com.justeat.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

        boolean existsByMenuItem(MenuItem menuItem);

        // Returns [menuItemId, totalQuantity] pairs for COMPLETED orders in a
        // restaurant
        @Query("SELECT oi.menuItem.id, SUM(oi.quantity) " +
                        "FROM OrderItem oi " +
                        "WHERE oi.order.restaurant.id = :restaurantId " +
                        "  AND oi.order.status = :status " +
                        "GROUP BY oi.menuItem.id")
        List<Object[]> sumQuantityByMenuItemAndStatus(
                        @Param("restaurantId") Long restaurantId,
                        @Param("status") OrderStatus status);
}
