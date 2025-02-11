package com.zipsoon.batch.job.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.dto.NaverResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonEstateItemReader implements ItemReader<NaverResponseDto> {

    private final ObjectMapper objectMapper;
    private boolean read = false;

    @Override
    public NaverResponseDto read() throws Exception {
        if (read) {
            return null;
        }

        Resource resource = new ClassPathResource("/mockdata/naver_response.json");

        try (InputStream inputStream = resource.getInputStream()) {
            read = true;
            return objectMapper.readValue(inputStream, NaverResponseDto.class);
        }
    }
}
