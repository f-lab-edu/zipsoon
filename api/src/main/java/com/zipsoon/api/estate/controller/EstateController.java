package com.zipsoon.api.estate.controller;

import com.zipsoon.api.estate.dto.EstateDetailResponse;
import com.zipsoon.api.estate.dto.EstateResponse;
import com.zipsoon.api.estate.dto.ViewportRequest;
import com.zipsoon.api.estate.service.EstateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estates")
@RequiredArgsConstructor
public class EstateController {
    private final EstateService estateService;

    @GetMapping("/map")
    public ResponseEntity<List<EstateResponse>> getEstatesInViewport(
        @Valid ViewportRequest request
    ) {
        return ResponseEntity.ok(
            estateService.findEstatesInViewport(request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstateDetailResponse> getEstateDetail(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            estateService.findEstateDetail(id)
        );
    }
}
