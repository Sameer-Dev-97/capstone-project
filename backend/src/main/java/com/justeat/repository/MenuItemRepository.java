package com.justeat.repository;

import com.justeat.entity.MenuItem;
import com.justeat.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndAvailableTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndTodaysSpecialTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndDealOfDayTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndMostOrderedTrue(Restaurant restaurant);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId ORDER BY m.orderCount DESC")
    List<MenuItem> findTopOrderedByRestaurant(Long restaurantId);
}
