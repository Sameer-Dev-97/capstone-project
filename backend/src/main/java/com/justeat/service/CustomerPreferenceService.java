package com.justeat.service;

import com.justeat.dto.CustomerPreferenceDTO;
import com.justeat.dto.RestaurantDTO;
import com.justeat.entity.CustomerPreference;
import com.justeat.entity.Restaurant;
import com.justeat.entity.User;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.repository.CustomerPreferenceRepository;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import com.justeat.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerPreferenceService {

        private static final Logger logger = LoggerFactory.getLogger(CustomerPreferenceService.class);

        private final CustomerPreferenceRepository preferenceRepository;
        private final UserRepository userRepository;
        private final RestaurantRepository restaurantRepository;
        private final SecurityUtil securityUtil;

        @Transactional
        public CustomerPreferenceDTO getPreferences() {
                String username = securityUtil.getCurrentUsername();
                logger.debug("Fetching preferences for user: {}", username);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                CustomerPreference preference = preferenceRepository.findByUser(user)
                                .orElseGet(() -> {
                                        CustomerPreference newPref = CustomerPreference.builder()
                                                        .user(user)
                                                        .favoriteRestaurants(new ArrayList<>())
                                                        .favoriteCuisines(new ArrayList<>())
                                                        .dietaryPreferences(new ArrayList<>())
                                                        .build();
                                        return preferenceRepository.save(newPref);
                                });

                return convertToDTO(preference);
        }

        @Transactional
        public CustomerPreferenceDTO updatePreferences(CustomerPreferenceDTO dto) {
                String username = securityUtil.getCurrentUsername();
                logger.info("Updating preferences for user: {}", username);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                CustomerPreference preference = preferenceRepository.findByUser(user)
                                .orElseGet(() -> {
                                        CustomerPreference newPref = CustomerPreference.builder()
                                                        .user(user)
                                                        .build();
                                        return preferenceRepository.save(newPref);
                                });

                preference.setFavoriteRestaurants(dto.getFavoriteRestaurants());
                preference.setFavoriteCuisines(dto.getFavoriteCuisines());
                preference.setDietaryPreferences(dto.getDietaryPreferences());

                preference = preferenceRepository.save(preference);
                logger.info("Preferences updated successfully");

                return convertToDTO(preference);
        }

        public List<RestaurantDTO> getRecommendations() {
                String username = securityUtil.getCurrentUsername();
                logger.debug("Generating recommendations for user: {}", username);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                CustomerPreference preference = preferenceRepository.findByUser(user)
                                .orElse(null);

                if (preference == null || preference.getFavoriteCuisines().isEmpty()) {
                        return restaurantRepository.findAll().stream()
                                        .limit(10)
                                        .map(this::convertRestaurantToDTO)
                                        .collect(Collectors.toList());
                }

                List<Restaurant> recommended = new ArrayList<>();
                for (String cuisine : preference.getFavoriteCuisines()) {
                        recommended.addAll(restaurantRepository.findByCuisineContainingIgnoreCase(cuisine));
                }

                return recommended.stream()
                                .distinct()
                                .limit(10)
                                .map(this::convertRestaurantToDTO)
                                .collect(Collectors.toList());
        }

        private CustomerPreferenceDTO convertToDTO(CustomerPreference preference) {
                return CustomerPreferenceDTO.builder()
                                .id(preference.getId())
                                .userId(preference.getUser().getId())
                                .favoriteRestaurants(preference.getFavoriteRestaurants())
                                .favoriteCuisines(preference.getFavoriteCuisines())
                                .dietaryPreferences(preference.getDietaryPreferences())
                                .build();
        }

        private RestaurantDTO convertRestaurantToDTO(Restaurant restaurant) {
                return RestaurantDTO.builder()
                                .id(restaurant.getId())
                                .name(restaurant.getName())
                                .location(restaurant.getLocation())
                                .cuisine(restaurant.getCuisine())
                                .imageUrl(restaurant.getImageUrl())
                                .rating(restaurant.getRating())
                                .ownerId(restaurant.getOwner().getId())
                                .build();
        }
}
