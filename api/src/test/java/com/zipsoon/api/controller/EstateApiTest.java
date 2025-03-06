package com.zipsoon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.api.application.estate.EstateService;
import com.zipsoon.api.application.estate.ScoreService;
import com.zipsoon.api.domain.auth.Role;
import com.zipsoon.api.domain.auth.UserPrincipal;
import com.zipsoon.api.domain.user.User;
import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.interfaces.api.estate.EstateController;
import com.zipsoon.api.interfaces.api.estate.dto.*;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateType;
import com.zipsoon.common.domain.TradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstateController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstateService estateService;

    @MockitoBean
    private ScoreService scoreService;

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final Long TEST_USER_ID = 1L;

    // 테스트 유틸리티 메서드 =========================================================

    private Point createPoint(double x, double y) {
        return geometryFactory.createPoint(new Coordinate(x, y));
    }

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

    // 점수 데이터 생성 헬퍼 메서드
    private ScoreDetails createScoreDetails(double totalScore, String description, int factorCount) {
        List<ScoreDetails.ScoreFactor> factors = IntStream.range(1, factorCount + 1)
            .mapToObj(i -> new ScoreDetails.ScoreFactor(
                (long) i,
                "점수 유형 " + i,
                "설명 " + i,
                7.0 + i))
            .toList();

        return new ScoreDetails(totalScore, description, factors);
    }

    private ScoreSummary createScoreSummary(double totalScore, int factorCount) {
        List<ScoreSummary.TopFactor> factors = IntStream.range(1, factorCount + 1)
            .mapToObj(i -> new ScoreSummary.TopFactor(
                (long) i,
                "점수 유형 " + i,
                7.0 + i))
            .toList();

        return new ScoreSummary(totalScore, factors);
    }

    private List<ScoreTypeResponse> createScoreTypes(boolean... enabledFlags) {
        List<ScoreTypeResponse> responses = new ArrayList<>();
        for (int i = 0; i < enabledFlags.length; i++) {
            responses.add(new ScoreTypeResponse(
                i + 1,
                "점수 유형 " + (i + 1),
                "설명 " + (i + 1),
                enabledFlags[i]
            ));
        }
        return responses;
    }

    private Estate createEstate(Long id) {
        return Estate.builder()
            .id(id)
            .estateName("테스트 매물 " + id)
            .estateType(EstateType.OR)
            .tradeType(TradeType.B2)
            .location(createPoint(127.0, 37.5))
            .build();
    }

    private EstateDetailResponse createEstateDetailResponse(Long id, ScoreDetails scoreDetails, boolean isFavorite) {
        return EstateDetailResponse.from(
            createEstate(id),
            scoreDetails,
            isFavorite
        );
    }

    private List<EstateResponse> createEstateResponses(int count, ScoreSummary scoreSummary) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> EstateResponse.from(
                createEstate((long) i),
                scoreSummary
            ))
            .toList();
    }

    // ViewportRequest 생성 헬퍼
    private ViewportRequest createViewportRequest() {
        return new ViewportRequest(126.0, 37.0, 127.0, 38.0, 15);
    }

    // 기본 테스트 =====================================================================

    @Test
    @DisplayName("매물이 없는 좌표 조회 시 404 Not Found 반환")
    void shouldReturnNotFound_When_NoEstatesInViewport() throws Exception {
        // given
        ViewportRequest request = createViewportRequest();
        when(estateService.findEstatesInViewport(any(), isNull()))
            .thenThrow(new ServiceException(ErrorCode.ESTATE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/estates/map")
                .param("swLng", String.valueOf(request.swLng()))
                .param("swLat", String.valueOf(request.swLat()))
                .param("neLng", String.valueOf(request.neLng()))
                .param("neLat", String.valueOf(request.neLat()))
                .param("zoom", String.valueOf(request.zoom())))
            .andExpect(status().isNotFound());

        // verify
        verify(estateService).findEstatesInViewport(any(), isNull());
    }

    @Nested
    @DisplayName("Score Type API 테스트")
    class ScoreTypeTests {

        @Test
        @DisplayName("비인증 사용자는 기본 점수 유형 목록을 조회할 수 있다")
        void shouldReturnAllScoreTypes_When_UserIsNotAuthenticated() throws Exception {
            // given
            List<ScoreTypeResponse> mockResponse = createScoreTypes(true, true);
            when(scoreService.getAllScoreTypes(null)).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/score-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("점수 유형 1"))
                .andExpect(jsonPath("$[0].enabled").value(true));

            // verify
            verify(scoreService).getAllScoreTypes(null);
        }

        @Test
        @DisplayName("인증된 사용자는 개인화된 점수 유형 목록을 조회할 수 있다")
        void shouldReturnPersonalizedScoreTypes_When_UserIsAuthenticated() throws Exception {
            // given
            List<ScoreTypeResponse> mockResponse = createScoreTypes(true, false);
            when(scoreService.getAllScoreTypes(eq(TEST_USER_ID))).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/score-types")
                    .with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].enabled").value(false));

            // verify
            verify(scoreService).getAllScoreTypes(eq(TEST_USER_ID));
        }

        @Test
        @DisplayName("인증된 사용자는 점수 유형을 비활성화할 수 있다")
        void shouldDisableScoreType_When_UserIsAuthenticated() throws Exception {
            // given
            int scoreTypeId = 2;
            doNothing().when(scoreService).disableScoreType(eq(TEST_USER_ID), eq(scoreTypeId));

            // when & then
            mockMvc.perform(post("/api/v1/estates/score-types/{scoreTypeId}/disable", scoreTypeId)
                    .with(withUserPrincipal()))
                .andExpect(status().isOk());

            // verify
            verify(scoreService).disableScoreType(TEST_USER_ID, scoreTypeId);
        }

        @Test
        @DisplayName("인증된 사용자는 점수 유형을 활성화할 수 있다")
        void shouldEnableScoreType_When_UserIsAuthenticated() throws Exception {
            // given
            int scoreTypeId = 2;
            doNothing().when(scoreService).enableScoreType(eq(TEST_USER_ID), eq(scoreTypeId));

            // when & then
            mockMvc.perform(post("/api/v1/estates/score-types/{scoreTypeId}/enable", scoreTypeId)
                    .with(withUserPrincipal()))
                .andExpect(status().isOk());

            // verify
            verify(scoreService).enableScoreType(TEST_USER_ID, scoreTypeId);
        }
    }

    @Nested
    @DisplayName("Estate API 테스트")
    class EstateTests {

        @Test
        @DisplayName("비인증 사용자가 매물 상세 조회시 기본 점수를 반환한다")
        void shouldReturnDefaultScores_When_GettingEstateDetailAsGuest() throws Exception {
            // given
            Long estateId = 1L;
            ScoreDetails mockScoreDetails = createScoreDetails(7.5, "총 3개 요소의 평균 점수입니다", 3);
            EstateDetailResponse mockResponse = createEstateDetailResponse(estateId, mockScoreDetails, false);

            when(estateService.findEstateDetail(estateId, null)).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/{id}", estateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estateId))
                .andExpect(jsonPath("$.score.total").value(7.5))
                .andExpect(jsonPath("$.score.factors.length()").value(3));

            // verify
            verify(estateService).findEstateDetail(estateId, null);
        }

        @Test
        @DisplayName("인증된 사용자가 매물 상세 조회시 개인화된 점수를 반환한다")
        void shouldReturnPersonalizedScores_When_GettingEstateDetailAsAuthenticatedUser() throws Exception {
            // given
            Long estateId = 1L;
            // 사용자가 '편의시설 점수'를 비활성화했다고 가정 - 2개 요소만 포함
            ScoreDetails mockScoreDetails = createScoreDetails(8.75, "총 2개 요소의 평균 점수입니다", 2);
            EstateDetailResponse mockResponse = createEstateDetailResponse(estateId, mockScoreDetails, false);

            when(estateService.findEstateDetail(eq(estateId), eq(TEST_USER_ID))).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/{id}", estateId)
                    .with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estateId))
                .andExpect(jsonPath("$.score.total").value(8.75))
                .andExpect(jsonPath("$.score.factors.length()").value(2));

            // verify
            verify(estateService).findEstateDetail(eq(estateId), eq(TEST_USER_ID));
        }

        @Test
        @DisplayName("비인증 사용자가 지도 매물 조회시 기본 점수를 반환한다")
        void shouldReturnDefaultScores_When_GettingEstatesInViewportAsGuest() throws Exception {
            // given
            ViewportRequest request = createViewportRequest();
            ScoreSummary scoreSummary = createScoreSummary(7.5, 3);
            List<EstateResponse> mockResponse = createEstateResponses(1, scoreSummary);

            when(estateService.findEstatesInViewport(any(ViewportRequest.class), isNull()))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/map")
                    .param("swLng", String.valueOf(request.swLng()))
                    .param("swLat", String.valueOf(request.swLat()))
                    .param("neLng", String.valueOf(request.neLng()))
                    .param("neLat", String.valueOf(request.neLat()))
                    .param("zoom", String.valueOf(request.zoom())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].score.total").value(7.5))
                .andExpect(jsonPath("$[0].score.topFactors.length()").value(3));

            // verify
            verify(estateService).findEstatesInViewport(any(ViewportRequest.class), isNull());
        }

        @Test
        @DisplayName("인증된 사용자가 지도 매물 조회시 개인화된 점수를 반환한다")
        void shouldReturnPersonalizedScores_When_GettingEstatesInViewportAsAuthenticatedUser() throws Exception {
            // given
            ViewportRequest request = createViewportRequest();
            // 사용자가 '편의시설 점수'를 비활성화했다고 가정 - 2개 요소만 포함
            ScoreSummary scoreSummary = createScoreSummary(8.75, 2);
            List<EstateResponse> mockResponse = createEstateResponses(1, scoreSummary);

            when(estateService.findEstatesInViewport(any(ViewportRequest.class), eq(TEST_USER_ID)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/estates/map")
                    .param("swLng", String.valueOf(request.swLng()))
                    .param("swLat", String.valueOf(request.swLat()))
                    .param("neLng", String.valueOf(request.neLng()))
                    .param("neLat", String.valueOf(request.neLat()))
                    .param("zoom", String.valueOf(request.zoom()))
                    .with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].score.total").value(8.75))
                .andExpect(jsonPath("$[0].score.topFactors.length()").value(2));

            // verify
            verify(estateService).findEstatesInViewport(any(ViewportRequest.class), eq(TEST_USER_ID));
        }
    }

    @Nested
    @DisplayName("찜하기 기능 테스트")
    class FavoriteTests {

        @Test
        @DisplayName("인증된 사용자가 매물을 찜할 수 있다")
        void shouldAddFavorite_When_AuthenticatedUserRequestsFavorite() throws Exception {
            // given
            Long estateId = 1L;
            doNothing().when(estateService).addFavorite(eq(estateId), eq(TEST_USER_ID));

            // when & then
            mockMvc.perform(post("/api/v1/estates/{id}/favorite", estateId)
                    .with(withUserPrincipal()))
                .andExpect(status().isCreated());

            // verify
            verify(estateService).addFavorite(estateId, TEST_USER_ID);
        }

        @Test
        @DisplayName("인증된 사용자가 매물 찜하기를 취소할 수 있다")
        void shouldRemoveFavorite_When_AuthenticatedUserRequestsUnfavorite() throws Exception {
            // given
            Long estateId = 1L;
            doNothing().when(estateService).removeFavorite(eq(estateId), eq(TEST_USER_ID));

            // when & then
            mockMvc.perform(delete("/api/v1/estates/{id}/favorite", estateId)
                    .with(withUserPrincipal()))
                .andExpect(status().isNoContent());

            // verify
            verify(estateService).removeFavorite(estateId, TEST_USER_ID);
        }

        @Test
        @DisplayName("존재하지 않는 매물 ID로 찜하기 요청하면 404 응답을 반환한다")
        void shouldReturnNotFound_When_RequestingNonExistentEstateFavorite() throws Exception {
            // given
            Long nonExistentEstateId = 999L;
            doThrow(new ServiceException(ErrorCode.ESTATE_NOT_FOUND))
                .when(estateService).addFavorite(eq(nonExistentEstateId), any());

            // when & then
            mockMvc.perform(post("/api/v1/estates/{id}/favorite", nonExistentEstateId)
                    .with(withUserPrincipal()))
                .andExpect(status().isNotFound());
        }
    }
}

