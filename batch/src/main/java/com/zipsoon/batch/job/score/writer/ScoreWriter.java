package com.zipsoon.batch.job.score.writer;

import com.zipsoon.batch.infrastructure.repository.score.ScoreRepository;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreWriter implements ItemWriter<List<EstateScore>> {
    private final ScoreRepository scoreRepository;

    @Override
    public void write(Chunk<? extends List<EstateScore>> chunk) {
        try {
            List<EstateScore> batchScores = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();
            
            // 배치 스코어 모델을 새 모델로 변환
            List<EstateScore> scores = convertToEstateScores(batchScores);
            
            // 최신 스코어 저장
            scoreRepository.saveAll(scores);
            
            // 오래된 스코어는 스냅샷으로 이동
            scoreRepository.migrateToScoreSnapshot();
            
            log.info("[BATCH:STEP-WRITER] 점수 저장 완료: {}개", scores.size());
        } catch (Exception e) {
            log.error("[BATCH:STEP-ERR] 점수 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("점수 저장 실패: " + e.getMessage(), e);
        }
    }
    
    // 배치용 EstateScore 모델을 공통 모델로 변환
    private List<EstateScore> convertToEstateScores(List<EstateScore> batchScores) {
        List<EstateScore> scores = new ArrayList<>();
        
        for (EstateScore batchScore : batchScores) {
            EstateScore score = EstateScore.builder()
                .estateId(batchScore.getEstateId())
                .scoreTypeId(batchScore.getScoreTypeId())
                .rawScore(batchScore.getRawScore())
                .normalizedScore(batchScore.getNormalizedScore())
                .createdAt(batchScore.getCreatedAt() != null ? batchScore.getCreatedAt() : LocalDateTime.now())
                .build();
            
            scores.add(score);
        }
        
        return scores;
    }
}