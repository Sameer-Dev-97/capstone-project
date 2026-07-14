package com.justeat.repository;

import com.justeat.entity.Restaurant;
import com.justeat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwner(User owner);

    @Query("SELECT r FROM Restaurant r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Restaurant> searchRestaurants(@Param("keyword") String keyword);

    List<Restaurant> findByCuisineContainingIgnoreCase(String cuisine);

    List<Restaurant> findByLocationContainingIgnoreCase(String location);
}
