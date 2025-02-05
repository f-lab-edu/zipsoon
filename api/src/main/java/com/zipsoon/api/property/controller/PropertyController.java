package com.zipsoon.api.property.controller;

import com.zipsoon.api.property.dto.PropertyDetailResponse;
import com.zipsoon.api.property.dto.PropertyResponse;
import com.zipsoon.api.property.dto.ViewportRequest;
import com.zipsoon.api.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping("/map")
    public ResponseEntity<List<PropertyResponse>> getPropertiesInViewport(
        @Valid ViewportRequest request
    ) {
        return ResponseEntity.ok(
            propertyService.findPropertiesInViewport(request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDetailResponse> getPropertyDetail(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            propertyService.findPropertyDetail(id)
        );
    }
}
