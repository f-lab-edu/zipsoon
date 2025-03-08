package com.zipsoon.batch.application.service.estate;

import com.zipsoon.common.domain.estate.DongCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DongCodeService {
//    private final DongCodeRepository dongCodeRepository;

    public List<DongCode> getAllDongCodes() {
//        List<DongCode> dongCodes = dongCodeRepository.findAll();
        List<DongCode> dongCodes = List.of(
            DongCode.of("1111000000", "서울특별시 종로구")
        );

        log.info("Retrieved {} dong codes", dongCodes.size());
        return dongCodes;
    }
}