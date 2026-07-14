package com.justeat.repository;

import com.justeat.entity.MenuItem;
import com.justeat.entity.MenuItemRating;
import com.justeat.entity.Order;
import com.justeat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemRatingRepository extends JpaRepository<MenuItemRating, Long> {

    Optional<MenuItemRating> findByCustomerAndMenuItem(User customer, MenuItem menuItem);

    Optional<MenuItemRating> findByCustomerAndOrderAndMenuItem(User customer, Order order, MenuItem menuItem);

    @Query("SELECT AVG(r.rating) FROM MenuItemRating r WHERE r.menuItem.id = :menuItemId")
    Optional<Double> findAverageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT AVG(r.rating) FROM MenuItemRating r WHERE r.menuItem.restaurant.id = :restaurantId")
    Optional<Double> findAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    boolean existsByCustomerAndMenuItem(User customer, MenuItem menuItem);

    boolean existsByCustomerAndOrderAndMenuItem(User customer, Order order, MenuItem menuItem);
}
