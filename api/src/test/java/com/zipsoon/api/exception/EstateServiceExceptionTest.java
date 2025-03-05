package com.zipsoon.api.exception;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstateServiceExceptionTest {

    @Mock
    private EstateService estateService;

    @Test
    @DisplayName("존재하지 않는 매물 ID로 조회하면 ESTATE_NOT_FOUND 예외 발생")
    void shouldThrowEstateNotFound_When_RequestingNonExistentEstateDetail() {
        when(estateService.findEstateDetail(any(), any()))
            .thenThrow(new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

        assertThrows(ServiceException.class, () -> estateService.findEstateDetail(9L, 999L));
    }
}