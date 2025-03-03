package com.zipsoon.batch;

import com.zipsoon.batch.job.estate.writer.EstateItemWriter;
import com.zipsoon.batch.infrastructure.repository.estate.BatchEstateRepository;
import com.zipsoon.batch.infrastructure.external.naver.NaverLandClient;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.PlatformType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBatchTest
class BatchExceptionHandlerTest {

    @Test
    @DisplayName("Naver API 요청 중 외부 API 호출 예외가 발생한다")
    void exceptionTest_NaverApiRequestFailure() {
        NaverLandClient naverLandClient = mock(NaverLandClient.class);
        when(naverLandClient.get(anyString(), anyInt()))
            .thenThrow(new RestClientException("외부 API 호출 실패"));

        assertThrows(RestClientException.class, () -> naverLandClient.get("1111018000", 1));
    }

    @Test
    @DisplayName("데이터 무결성 위반으로 유효성 예외가 발생한다")
    void exceptionTest_DataIntegrityViolation() {
        BatchEstateRepository repository = mock(BatchEstateRepository.class);
        EstateItemWriter writer = new EstateItemWriter(repository);

        List<Estate> snapshots = List.of(
            Estate.builder()
                .platformType(PlatformType.네이버)
                .platformId("SAME_ID")
                .build(),
            Estate.builder()
                .platformType(PlatformType.네이버)
                .platformId("SAME_ID")
                .build()
        );

        doThrow(new DataIntegrityViolationException("Duplicate key"))
            .when(repository).saveAllEstates(eq(snapshots));

        assertThrows(IllegalArgumentException.class, () -> writer.write(new Chunk<>(List.of(snapshots))));
    }

}