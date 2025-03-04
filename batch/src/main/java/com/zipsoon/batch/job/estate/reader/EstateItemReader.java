package com.zipsoon.batch.job.estate.reader;

import com.zipsoon.batch.domain.estate.DongCode;
import com.zipsoon.batch.application.service.estate.DongCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateItemReader implements ItemReader<String> {
    private final DongCodeService dongCodeService;
    
    private List<String> dongCodes;
    private int currentIndex = 0;

    @Override
    public String read() {
        if (dongCodes == null) {
            this.dongCodes = dongCodeService.getAllDongCodes()
                .stream()
                .map(DongCode::code)
                .toList();
            log.info("Estate reader initialized with {} dong codes", dongCodes.size());
        }
        
        if (currentIndex < dongCodes.size()) {
            return dongCodes.get(currentIndex++);
        }
        return null;
    }
}
