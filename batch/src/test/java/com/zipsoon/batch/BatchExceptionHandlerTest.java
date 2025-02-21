package com.zipsoon.batch;

import com.zipsoon.batch.estate.job.writer.EstateItemWriter;
import com.zipsoon.batch.estate.repository.EstateSnapshotRepository;
import com.zipsoon.batch.infra.naver.NaverLandClient;
import com.zipsoon.common.domain.EstateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBatchTest
class BatchExceptionHandlerTest {

    @Test
    @DisplayName("Naver API 요청 중 외부 API 호출 예외가 발생한다")
    void exceptionTest_NaverApiRequestFailure() {
        NaverLandClient naverLandClient = mock(NaverLandClient.class);
        when(naverLandClient.get(anyString(), anyInt()))
            .thenThrow(new RestClientException("외부 API 호출 실패"));

        assertThrows(RestClientException.class, () -> {
            naverLandClient.get("1111018000", 1);
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

        assertThrows(IllegalArgumentException.class, () -> {
            writer.write(new Chunk<>(List.of(snapshots)));
        });
    }

}