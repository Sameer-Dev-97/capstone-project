package com.justeat.controller;

import com.justeat.dto.CustomerPreferenceDTO;
import com.justeat.dto.RestaurantDTO;
import com.justeat.service.CustomerPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Customer Preferences", description = "Customer preference APIs")
public class CustomerPreferenceController {

    private final CustomerPreferenceService preferenceService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get customer preferences (Customer only)")
    public ResponseEntity<CustomerPreferenceDTO> getPreferences() {
        return ResponseEntity.ok(preferenceService.getPreferences());
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update customer preferences (Customer only)")
    public ResponseEntity<CustomerPreferenceDTO> updatePreferences(@RequestBody CustomerPreferenceDTO dto) {
        return ResponseEntity.ok(preferenceService.updatePreferences(dto));
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get restaurant recommendations (Customer only)")
    public ResponseEntity<List<RestaurantDTO>> getRecommendations() {
        return ResponseEntity.ok(preferenceService.getRecommendations());
    }
}
