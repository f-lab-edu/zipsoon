package com.zipsoon.batch.infrastructure.repository.estate;

import com.zipsoon.batch.domain.estate.DongCode;
import com.zipsoon.batch.infrastructure.mapper.estate.DongCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DongCodeRepository {
    private final DongCodeMapper dongCodeMapper;

    public List<DongCode> findAll() {
        return dongCodeMapper.findAll();
    }
}
