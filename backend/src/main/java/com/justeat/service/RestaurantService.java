package com.justeat.service;

import com.justeat.dto.RestaurantDTO;
import com.justeat.dto.RestaurantRequest;
import com.justeat.entity.Restaurant;
import com.justeat.entity.Role;
import com.justeat.entity.User;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.exception.UnauthorizedException;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import com.justeat.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public List<RestaurantDTO> getAllRestaurants() {
        logger.debug("Fetching all restaurants");
        return restaurantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> searchRestaurants(String keyword) {
        logger.debug("Searching restaurants with keyword: {}", keyword);
        return restaurantRepository.searchRestaurants(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RestaurantDTO getRestaurantById(Long id) {
        logger.debug("Fetching restaurant by ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        return convertToDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO createRestaurant(RestaurantRequest request) {
        String username = securityUtil.getCurrentUsername();
        logger.info("Creating restaurant for user: {}", username);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (owner.getRole() != Role.RESTAURANT_OWNER) {
            throw new UnauthorizedException("Only restaurant owners can create restaurants");
        }

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .location(request.getLocation())
                .cuisine(request.getCuisine())
                .imageUrl(normalizeImageUrl(request.getImageUrl()))
                .rating(0.0)
                .owner(owner)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant created successfully: {}", restaurant.getName());

        return convertToDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO updateRestaurant(Long id, RestaurantRequest request) {
        String username = securityUtil.getCurrentUsername();
        logger.info("Updating restaurant ID: {} by user: {}", id, username);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update your own restaurants");
        }

        restaurant.setName(request.getName());
        restaurant.setLocation(request.getLocation());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setImageUrl(normalizeImageUrl(request.getImageUrl()));

        restaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant updated successfully: {}", restaurant.getName());

        return convertToDTO(restaurant);
    }

    public List<RestaurantDTO> getMyRestaurants() {
        String username = securityUtil.getCurrentUsername();
        logger.debug("Fetching restaurants for owner: {}", username);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return restaurantRepository.findByOwner(owner).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RestaurantDTO convertToDTO(Restaurant restaurant) {
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

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        return imageUrl.trim();
    }
}
