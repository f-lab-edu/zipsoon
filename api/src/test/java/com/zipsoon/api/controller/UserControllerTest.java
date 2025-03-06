package com.zipsoon.api.controller;

import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.user.UserService;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.interfaces.api.common.dto.PageResponse;
import com.zipsoon.api.interfaces.api.estate.dto.EstateResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreSummary;
import com.zipsoon.api.interfaces.api.user.UserController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstateService estateService;

    @MockitoBean
    private UserService userService;

    private final Long TEST_USER_ID = 1L;

    // 테스트 유틸리티 메서드
    private UserPrincipal createMockUserPrincipal() {
        User user = User.builder()
            .id(TEST_USER_ID)
            .email("test@example.com")
            .role(Role.USER)
            .build();
        return UserPrincipal.create(user);
    }

    // 커스텀 인증 설정으로 UserPrincipal 사용
    private RequestPostProcessor withUserPrincipal() {
        return request -> {
            UserPrincipal userPrincipal = createMockUserPrincipal();

            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userPrincipal, "", userPrincipal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(token);
            request.setUserPrincipal(token);
            return request;
        };
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private PageResponse<EstateResponse> createMockPageResponse() {
        // 테스트용 점수 정보 생성
        ScoreSummary scoreSummary = new ScoreSummary(8.5, List.of(
            new ScoreSummary.TopFactor(1L, "교통", 9.0),
            new ScoreSummary.TopFactor(2L, "편의시설", 8.0)
        ));

        // 테스트용 매물 응답 생성
        List<EstateResponse> estates = List.of(
            new EstateResponse(
                1L, "테스트 매물 1", null, null,
                BigDecimal.valueOf(100000000), null,
                BigDecimal.valueOf(84.5), 37.5, 127.0, scoreSummary
            ),
            new EstateResponse(
                2L, "테스트 매물 2", null, null,
                BigDecimal.valueOf(150000000), BigDecimal.valueOf(1500000),
                BigDecimal.valueOf(23.14), 37.51, 127.01, scoreSummary
            )
        );

        return new PageResponse<>(estates, 0, 10, 2, 1);
    }

    @Test
    @DisplayName("인증된 사용자가 찜한 매물 목록을 조회할 수 있다")
    void shouldReturnFavoriteEstates_When_AuthenticatedUserRequestsFavorites() throws Exception {
        // given
        PageResponse<EstateResponse> mockResponse = createMockPageResponse();
        when(estateService.findFavoriteEstates(eq(TEST_USER_ID), anyInt(), anyInt()))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/users/me/favorites")
                .param("page", "0")
                .param("size", "10")
                .with(withUserPrincipal()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("테스트 매물 1"))
            .andExpect(jsonPath("$.content[0].score.total").value(8.5))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));

        // verify
        verify(estateService).findFavoriteEstates(eq(TEST_USER_ID), eq(0), eq(10));
    }

    @Test
    @DisplayName("페이지 파라미터를 지정하지 않으면 기본값을 사용한다")
    void shouldUseDefaultPagination_When_ParametersNotProvided() throws Exception {
        // given
        PageResponse<EstateResponse> mockResponse = createMockPageResponse();
        when(estateService.findFavoriteEstates(eq(TEST_USER_ID), anyInt(), anyInt()))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/users/me/favorites")
                .with(withUserPrincipal()))
            .andExpect(status().isOk());

        // verify - 기본값 page=0, size=10 사용
        verify(estateService).findFavoriteEstates(eq(TEST_USER_ID), eq(0), eq(10));
    }

    @Test
    @DisplayName("유효하지 않은 페이지 파라미터를 입력하면 400 Bad Request를 반환한다")
    void shouldReturnBadRequest_When_InvalidPaginationParametersProvided() throws Exception {
        // when & then - 음수 페이지 요청
        mockMvc.perform(get("/api/v1/users/me/favorites")
                .param("page", "-1")
                .param("size", "10")
                .with(withUserPrincipal()))
            .andExpect(status().isBadRequest());

        // when & then - 너무 큰 size 요청
        mockMvc.perform(get("/api/v1/users/me/favorites")
                .param("page", "0")
                .param("size", "1000")
                .with(withUserPrincipal()))
            .andExpect(status().isBadRequest());
    }
}