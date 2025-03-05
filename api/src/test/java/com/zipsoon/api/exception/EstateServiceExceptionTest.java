package com.zipsoon.api.exception;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.estate.dto.ViewportRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstateServiceExceptionTest {

    @Mock
    private EstateService estateService;

    @Nested
    @DisplayName("매물 상세 조회 예외 테스트")
    class EstateDetailTests {

        @Test
        @DisplayName("존재하지 않는 매물 ID로 조회하면 ESTATE_NOT_FOUND 예외 발생")
        void shouldThrowEstateNotFound_When_RequestingNonExistentEstateDetail() {
            // Given
            when(estateService.findEstateDetail(anyLong(), any()))
                .thenThrow(new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                () -> estateService.findEstateDetail(9L, 999L));

            // Verify
            assertEquals(ErrorCode.ESTATE_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("뷰포트 매물 조회 예외 테스트")
    class ViewportTests {

        @Test
        @DisplayName("잘못된 뷰포트 좌표로 조회하면 BAD_REQUEST 예외 발생")
        void shouldThrowBadRequest_When_ViewportCoordinatesAreInvalid() {
            // Given: 경도 좌표가 잘못된 요청 (서쪽 경도가 동쪽 경도보다 큼)
            ViewportRequest invalidRequest = new ViewportRequest(
                127.0, 37.0,
                126.0, 38.0,
                15
            );

            when(estateService.findEstatesInViewport(invalidRequest, null))
                .thenThrow(new ServiceException(ErrorCode.BAD_REQUEST, "뷰포트 좌표가 유효하지 않습니다."));

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                () -> estateService.findEstatesInViewport(invalidRequest, null));

            // Verify
            assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        }

        @Test
        @DisplayName("너무 넓은 뷰포트로 검색하면 BAD_REQUEST 예외 발생")
        void shouldThrowBadRequest_When_ViewportIsTooLarge() {
            // Given: 줌 레벨에 비해 너무 넓은 뷰포트
            ViewportRequest tooLargeRequest = new ViewportRequest(
                126.0, 35.0,
                130.0, 39.0,
                5
            );

            when(estateService.findEstatesInViewport(tooLargeRequest, null))
                .thenThrow(new ServiceException(ErrorCode.BAD_REQUEST, "해당 확대 레벨에서는 뷰포트 크기가 너무 큽니다."));

            // When & Then
            ServiceException exception = assertThrows(ServiceException.class,
                () -> estateService.findEstatesInViewport(tooLargeRequest, null));

            // Verify
            assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        }
    }

}