package com.zipsoon.api.application.estate;

import com.zipsoon.api.interfaces.api.estate.dto.ScoreDetails;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreDto;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreSummary;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreTypeResponse;
import com.zipsoon.api.infrastructure.repository.estate.ApiScoreRepository;
import com.zipsoon.api.infrastructure.repository.user.UserDisabledScoreTypeRepository;
import com.zipsoon.api.domain.user.UserDisabledScoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ApiScoreRepository apiScoreRepository;
    private final UserDisabledScoreTypeRepository userDisabledScoreTypeRepository;
    
    /**
     * 매물의 점수 요약 정보를 조회합니다. 로그인한 사용자인 경우 비활성화된 점수 유형을 제외합니다.
     * 
     * @param estateId 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 점수 요약 정보
     */
    @Transactional(readOnly = true)
    public ScoreSummary getScoreSummary(Long estateId, Long userId) {
        var scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            return new ScoreSummary(0.0, List.of());
        }
        
        // 로그인한 사용자인 경우 비활성화된 점수 유형 필터링
        if (userId != null) {
            Set<Integer> disabledScoreTypeIds = getDisabledScoreTypeIds(userId);
            
            if (!disabledScoreTypeIds.isEmpty()) {
                scoreFactors = scoreFactors.stream()
                    .filter(factor -> !disabledScoreTypeIds.contains(factor.getScoreTypeId()))
                    .collect(Collectors.toList());
                
                if (scoreFactors.isEmpty()) {
                    return new ScoreSummary(0.0, List.of());
                }
            }
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
    
    /**
     * 기존 호환성을 위한 오버로딩 메서드
     */
    @Transactional(readOnly = true)
    public ScoreSummary getScoreSummary(Long estateId) {
        return getScoreSummary(estateId, null);
    }
    
    /**
     * 매물의 상세 점수 정보를 조회합니다. 로그인한 사용자인 경우 비활성화된 점수 유형을 제외합니다.
     * 
     * @param estateId 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 상세 점수 정보
     */
    @Transactional(readOnly = true)
    public ScoreDetails getScoreDetails(Long estateId, Long userId) {
        var scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            return new ScoreDetails(0.0, "점수 정보가 없습니다", List.of());
        }
        
        // 로그인한 사용자인 경우 비활성화된 점수 유형 필터링
        if (userId != null) {
            Set<Integer> disabledScoreTypeIds = getDisabledScoreTypeIds(userId);
            
            if (!disabledScoreTypeIds.isEmpty()) {
                scoreFactors = scoreFactors.stream()
                    .filter(factor -> !disabledScoreTypeIds.contains(factor.getScoreTypeId()))
                    .collect(Collectors.toList());
                
                if (scoreFactors.isEmpty()) {
                    return new ScoreDetails(0.0, "모든 점수 유형이 비활성화되었습니다", List.of());
                }
            }
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
    
    /**
     * 사용자가 비활성화한 점수 유형 ID 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 비활성화된 점수 유형 ID 세트
     */
    private Set<Integer> getDisabledScoreTypeIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        
        List<Integer> disabledScoreTypeIds = userDisabledScoreTypeRepository.findDisabledScoreTypeIdsByUserId(userId);
        return Set.copyOf(disabledScoreTypeIds);
    }
    
    /**
     * 사용자의 점수 유형 비활성화 설정을 추가합니다.
     * 
     * @param userId 사용자 ID
     * @param scoreTypeId 점수 유형 ID
     */
    @Transactional
    public void disableScoreType(Long userId, Integer scoreTypeId) {
        UserDisabledScoreType disabledScoreType = UserDisabledScoreType.create(userId, scoreTypeId);
        userDisabledScoreTypeRepository.insert(disabledScoreType);
    }
    
    /**
     * 사용자의 점수 유형 비활성화 설정을 제거합니다.
     * 
     * @param userId 사용자 ID
     * @param scoreTypeId 점수 유형 ID
     */
    @Transactional
    public void enableScoreType(Long userId, Integer scoreTypeId) {
        userDisabledScoreTypeRepository.delete(userId, scoreTypeId);
    }
    
    /**
     * 사용자의 점수 유형 목록을 조회합니다.
     * 비활성화된 점수 유형은 enabled=false로 표시됩니다.
     *
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 점수 유형 목록
     */
    @Transactional(readOnly = true)
    public List<ScoreTypeResponse> getAllScoreTypes(Long userId) {
        // 모든 점수 유형 조회
        List<Map<String, Object>> scoreTypes = apiScoreRepository.findAllScoreTypes();
        
        // 비활성화된 점수 유형 ID 조회
        Set<Integer> disabledScoreTypeIds = 
            (userId != null) ? getDisabledScoreTypeIds(userId) : Collections.emptySet();
        
        // 점수 유형 응답 생성
        return scoreTypes.stream()
            .map(scoreType -> {
                Integer id = (Integer) scoreType.get("id");
                String name = (String) scoreType.get("name");
                String description = (String) scoreType.get("description");
                
                // 비활성화 목록에 있으면 enabled=false, 없으면 enabled=true
                boolean enabled = !disabledScoreTypeIds.contains(id);
                
                return ScoreTypeResponse.of(id, name, description, enabled);
            })
            .collect(Collectors.toList());
    }
    
    private double calculateTotalScore(List<ScoreDto> factors) {
        return factors.stream()
            .filter(factor -> factor.getNormalizedScore() != null)
            .mapToDouble(ScoreDto::getNormalizedScore)
            .average()
            .orElse(0.0);
    }
}