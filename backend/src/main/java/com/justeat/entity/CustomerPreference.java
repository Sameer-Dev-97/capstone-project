package com.justeat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "favorite_restaurants", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "restaurant_id")
    private List<Long> favoriteRestaurants = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "favorite_cuisines", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "cuisine")
    private List<String> favoriteCuisines = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "dietary_preferences", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "preference")
    private List<String> dietaryPreferences = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
