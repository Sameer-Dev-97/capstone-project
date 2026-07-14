package com.justeat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDTO {

    private Long id;
    private String name;
    private String location;
    private String cuisine;
    private String imageUrl;
    private Double rating;
    private Long ownerId;
}
