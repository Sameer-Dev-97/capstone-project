package com.justeat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    private Boolean todaysSpecial;

    @Column(nullable = false)
    private Boolean dealOfDay;

    @Column(nullable = false)
    private Boolean mostOrdered;

    @Column(nullable = false)
    private Integer orderCount;

    @Column
    private Double rating;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemRating> ratings = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (available == null)
            available = true;
        if (todaysSpecial == null)
            todaysSpecial = false;
        if (dealOfDay == null)
            dealOfDay = false;
        if (mostOrdered == null)
            mostOrdered = false;
        if (orderCount == null)
            orderCount = 0;
        if (rating == null)
            rating = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
