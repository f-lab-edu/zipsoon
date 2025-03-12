package com.zipsoon.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 집순 Batch 애플리케이션 메인 클래스
 * 
 * <p>매물 수집, 점수 계산, 정규화 등의 배치 작업을 처리하는 애플리케이션입니다.
 *
 * <p>주요 배치 작업:
 * <ul>
 *   <li>매물 수집 (Estate Collection Job)</li>
 *   <li>점수 소스 데이터 수집 (Source Collection Job)</li>
 *   <li>매물 점수 계산 (Score Calculation Job)</li>
 *   <li>점수 정규화 (Score Normalization Job)</li>
 * </ul>
 * </p>
 */
@SpringBootApplication(scanBasePackages = "com.zipsoon")
@EnableBatchProcessing
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
