package com.zipsoon.batch.service;

import com.zipsoon.batch.domain.DongCode;
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
            new DongCode("1111018000", "서울특별시 종로구 교북동")
        );

        log.info("Retrieved {} dong codes", dongCodes.size());
        return dongCodes;
    }
}