package com.justeat.repository;

import com.justeat.entity.CartItem;
import com.justeat.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteByMenuItem(MenuItem menuItem);
}
