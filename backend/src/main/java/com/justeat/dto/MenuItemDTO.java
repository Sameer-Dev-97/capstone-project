package com.justeat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Boolean available;
    private Boolean todaysSpecial;
    private Boolean dealOfDay;
    private Boolean mostOrdered;
    private Integer orderCount;
    private Double rating;
    private Long restaurantId;
}
