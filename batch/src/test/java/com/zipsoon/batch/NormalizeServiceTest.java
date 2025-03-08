package com.zipsoon.batch;

import com.zipsoon.batch.job.normalize.writer.NormalizeWriter;
import com.zipsoon.batch.application.service.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.batch.infrastructure.repository.normalize.NormalizeRepository;
import com.zipsoon.batch.application.service.score.calculator.ScoreCalculator;
import com.zipsoon.batch.domain.score.ScoreType;
import com.zipsoon.common.domain.EstateScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class NormalizeServiceTest {
    private NormalizeRepository normalizeRepository;
    private ScoreNormalizer mockNormalizer;
    private ScoreCalculator mockCalculator;
    private NormalizeWriter normalizeWriter;

    @BeforeEach
    void setUp() {
        normalizeRepository = mock(NormalizeRepository.class);

        mockNormalizer = mock(ScoreNormalizer.class);

        mockCalculator = mock(ScoreCalculator.class);
        when(mockCalculator.getScoreId()).thenReturn(1L);
        when(mockCalculator.getNormalizer()).thenReturn(mockNormalizer);

        normalizeWriter = new NormalizeWriter(normalizeRepository, List.of(mockCalculator));
    }

    @Test
    @DisplayName("정규화 프로세스는 원시 점수를 입력받아 정규화된 점수를 생성한다")
    void normalizationProcessTransformsRawScores() {
        // given
        ScoreType scoreType = ScoreType.builder()
            .id(1L)
            .name("테스트 점수")
            .active(true)
            .build();

        List<EstateScore> scores = Arrays.asList(
            EstateScore.builder().id(1L).scoreTypeId(1L).rawScore(3.0).build(),
            EstateScore.builder().id(2L).scoreTypeId(1L).rawScore(7.0).build()
        );

        when(normalizeRepository.findByScoreTypeId(1L)).thenReturn(scores);
        when(mockNormalizer.normalize(eq(3.0), anyList())).thenReturn(2.5);
        when(mockNormalizer.normalize(eq(7.0), anyList())).thenReturn(8.5);

        // when
        normalizeWriter.write(new Chunk<>(List.of(scoreType)));

        // then
        verify(normalizeRepository).updateNormalizedScores(eq(1L), argThat(map -> map.size() == 2));
    }

    @Test
    @DisplayName("정규화기가 없는 경우 해당 점수 유형을 건너뛴다")
    void skipsScoreTypeWithoutNormalizer() {
        // given
        ScoreCalculator calculatorWithoutNormalizer = mock(ScoreCalculator.class);
        when(calculatorWithoutNormalizer.getScoreId()).thenReturn(2L);
        when(calculatorWithoutNormalizer.getNormalizer()).thenReturn(null);

        NormalizeWriter writer = new NormalizeWriter(
            normalizeRepository,
            List.of(mockCalculator, calculatorWithoutNormalizer)
        );

        ScoreType scoreType = ScoreType.builder()
            .id(2L)
            .name("정규화기 없는 점수")
            .active(true)
            .build();

        // when
        writer.write(new Chunk<>(List.of(scoreType)));

        // then
        verify(normalizeRepository, never()).findByScoreTypeId(2L);
        verify(normalizeRepository, never()).updateNormalizedScores(eq(2L), anyMap());
    }

    @Test
    @DisplayName("점수 데이터가 없는 경우 정규화를 시도하지 않는다")
    void doesNotNormalizeWhenNoScoresExist() {
        // given
        ScoreType scoreType = ScoreType.builder()
            .id(1L)
            .name("데이터 없는 점수")
            .active(true)
            .build();

        when(normalizeRepository.findByScoreTypeId(1L)).thenReturn(List.of());

        // when
        normalizeWriter.write(new Chunk<>(List.of(scoreType)));

        // then
        verify(normalizeRepository).findByScoreTypeId(1L);
        verify(normalizeRepository, never()).updateNormalizedScores(eq(1L), anyMap());
    }
}