package com.justeat.repository;

import com.justeat.entity.Order;
import com.justeat.entity.OrderStatus;
import com.justeat.entity.Restaurant;
import com.justeat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerOrderByOrderDateDesc(User customer);

    List<Order> findByRestaurantOrderByOrderDateDesc(Restaurant restaurant);

    List<Order> findByRestaurant(Restaurant restaurant);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items oi " +
            "WHERE o.customer = :customer AND oi.menuItem.id = :menuItemId AND o.status = :status")
    boolean existsByCustomerAndMenuItemIdAndStatus(
            @Param("customer") User customer,
            @Param("menuItemId") Long menuItemId,
            @Param("status") OrderStatus status);
}
