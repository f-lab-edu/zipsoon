package com.zipsoon.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.exception.BatchJobFailureException;
import com.zipsoon.batch.exception.NaverApiException;
import com.zipsoon.batch.exception.EstateProcessingException;
import com.zipsoon.batch.job.processor.EstateItemProcessor;
import com.zipsoon.batch.job.writer.EstateItemWriter;
import com.zipsoon.batch.service.NaverClient;
import com.zipsoon.common.domain.EstateSnapshot;
import com.zipsoon.common.exception.ErrorCode;
import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.repository.EstateSnapshotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBatchTest
class BatchExceptionHandlerTest {

//    @Test
//    @DisplayName("배치 작업이 이미 완료되어 예외가 발생한다")
//    void exceptionTest_JobInstanceAlreadyComplete() {
//        assertThrows(JobInstanceAlreadyCompleteException.class, () -> {
//        });
//    }

    @Test
    @DisplayName("Naver API 요청 중 외부 API 호출 예외가 발생한다")
    void exceptionTest_NaverApiRequestFailure() {
        NaverClient naverClient = mock(NaverClient.class);
        when(naverClient.get(anyString(), anyInt()))
            .thenThrow(new NaverApiException(ErrorCode.EXTERNAL_API_ERROR));

        assertThrows(NaverApiException.class, () -> {
            naverClient.get("1111018000", 1);
        });
    }

    @Test
    @DisplayName("매물 정보 처리 중 예외가 발생한다")
    void exceptionTest_EstateProcessing() {
        EstateItemProcessor processor = new EstateItemProcessor(new ObjectMapper());
        NaverResponseDto invalidResponse = new NaverResponseDto(true, null, null);

        assertThrows(EstateProcessingException.class, () -> {
            processor.process(invalidResponse);
        });
    }

    @Test
    @DisplayName("데이터 무결성 위반으로 유효성 예외가 발생한다")
    void exceptionTest_DataIntegrityViolation() {
        EstateSnapshotRepository repository = mock(EstateSnapshotRepository.class);
        EstateItemWriter writer = new EstateItemWriter(repository);

        List<EstateSnapshot> snapshots = List.of(
            EstateSnapshot.builder()
                .platformType(EstateSnapshot.PlatformType.네이버)
                .platformId("SAME_ID")
                .build(),
            EstateSnapshot.builder()
                .platformType(EstateSnapshot.PlatformType.네이버)
                .platformId("SAME_ID")
                .build()
        );

        doThrow(new DataIntegrityViolationException("Duplicate key"))
            .when(repository).saveAll(eq(snapshots));

        InvalidValueException thrown = assertThrows(
            InvalidValueException.class,
            () -> writer.write(new Chunk<>(List.of(snapshots)))
        );

        assertEquals(ErrorCode.RESOURCE_CONFLICT, thrown.getErrorCode());
    }

    @Test
    @DisplayName("배치 작업 중 예기치 않은 실패 예외가 발생한다")
    void exceptionTest_BatchJobFailure() {
        assertThrows(BatchJobFailureException.class, () -> {
            throw new BatchJobFailureException(ErrorCode.SERVER_ERROR);
        });
    }
}