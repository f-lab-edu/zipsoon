// api/src/main/java/com/zipsoon/api/estate/service/ScoreService.java

package com.zipsoon.api.estate.service;

import com.zipsoon.api.estate.dto.ScoreDetails;
import com.zipsoon.api.estate.dto.ScoreDto;
import com.zipsoon.api.estate.dto.ScoreSummary;
import com.zipsoon.api.estate.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ScoreMapper scoreMapper;
    
    @Transactional(readOnly = true)
    public ScoreSummary getScoreSummary(Long estateId) {
        var scoreFactors = scoreMapper.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            return new ScoreSummary(0.0, List.of());
        }
        
        double totalScore = calculateTotalScore(scoreFactors);
        var topFactors = scoreFactors.stream()
            .sorted((f1, f2) -> Double.compare(f2.getNormalizedScore(), f1.getNormalizedScore()))
            .limit(3)
            .map(factor -> new ScoreSummary.TopFactor(
                factor.getScoreTypeId(),
                factor.getScoreTypeName(),
                factor.getNormalizedScore()
            ))
            .toList();
            
        return new ScoreSummary(totalScore, topFactors);
    }
    
    @Transactional(readOnly = true)
    public ScoreDetails getScoreDetails(Long estateId) {
        var scoreFactors = scoreMapper.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            return new ScoreDetails(0.0, "점수 정보가 없습니다", List.of());
        }
        
        double totalScore = calculateTotalScore(scoreFactors);
        var factors = scoreFactors.stream()
            .map(factor -> new ScoreDetails.ScoreFactor(
                factor.getScoreTypeId(),
                factor.getScoreTypeName(),
                factor.getDescription(),
                factor.getNormalizedScore()
            ))
            .toList();
            
        return new ScoreDetails(
            totalScore,
            String.format("총 %d개 요소의 평균 점수입니다", factors.size()),
            factors
        );
    }
    
    private double calculateTotalScore(List<ScoreDto> factors) {
        return factors.stream()
            .mapToDouble(ScoreDto::getNormalizedScore)
            .average()
            .orElse(0.0);
    }
}