package com.zipsoon.batch.score.repository;

import com.zipsoon.batch.score.mapper.ScoreTypeMapper;
import com.zipsoon.batch.score.model.ScoreType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScoreTypeRepository {
    private final ScoreTypeMapper scoreTypeMapper;

    public List<ScoreType> findAllActive() {
        return scoreTypeMapper.findAllActive();
    }

    public Optional<ScoreType> findByName(String name) {
        return scoreTypeMapper.findByName(name);
    }
}