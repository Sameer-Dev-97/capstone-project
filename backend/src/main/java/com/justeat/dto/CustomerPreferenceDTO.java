package com.justeat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPreferenceDTO {

    private Long id;
    private Long userId;
    private List<Long> favoriteRestaurants;
    private List<String> favoriteCuisines;
    private List<String> dietaryPreferences;
}
