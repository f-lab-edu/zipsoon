package com.zipsoon.api.infrastructure.repository.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import com.zipsoon.api.infrastructure.mapper.estate.ApiEstateMapper;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApiEstateRepository {
    private final ApiEstateMapper mapper;
    private static final int SRID = 4326; // WGS84 좌표계 SRID 값

    public List<Estate> findAllInViewport(ViewportRequest viewport, int limit) {
        return mapper.selectAllInViewport(viewport, limit, SRID);
    }

    public Optional<Estate> findById(Long id) {
        return mapper.selectById(id);
    }
}