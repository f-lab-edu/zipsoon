package com.zipsoon.api.infrastructure.repository.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import com.zipsoon.api.interfaces.mapper.ApiEstateMapper;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApiEstateRepository {
    private final ApiEstateMapper apiEstateMapper;
    private static final int SRID = 4326; // WGS84 좌표계 SRID 값

    public List<Estate> findAllInViewport(ViewportRequest viewport, int limit) {
        return apiEstateMapper.findAllInViewport(viewport, limit, SRID);
    }

    public Optional<Estate> findById(Long id) {
        return apiEstateMapper.findById(id);
    }
}