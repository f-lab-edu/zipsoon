package com.zipsoon.api.application.estate;

import com.zipsoon.api.domain.user.UserDisabledScoreType;
import com.zipsoon.api.infrastructure.repository.estate.ApiScoreRepository;
import com.zipsoon.api.infrastructure.repository.user.UserDisabledScoreTypeRepository;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreDetails;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreDto;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreSummary;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreTypeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ApiScoreRepository apiScoreRepository;
    private final UserDisabledScoreTypeRepository userDisabledScoreTypeRepository;

    /**
     * 매물의 점수 요약 정보를 조회합니다.
     *
     * @param estateId 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 점수 요약 정보
     */
    @Transactional(readOnly = true)
    public ScoreSummary getScoreSummary(Long estateId, Long userId) {
        log.debug("Getting score summary for estate: {} (userId: {})", estateId, userId != null ? userId : "guest");

        // 기본 점수 정보 조회
        List<ScoreDto> scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            log.debug("No score factors found for estate: {}", estateId);
            return new ScoreSummary(0.0, List.of());
        }

        // 사용자 설정에 따라 필터링된 요소 목록 준비
        List<ScoreDto> filteredFactors = filterScoreFactorsByUserPreferences(scoreFactors, userId);
        if (filteredFactors.isEmpty()) {
            log.info("All score types are disabled by user: {}", userId);
            return new ScoreSummary(0.0, List.of());
        }

        // 총점 계산
        double totalScore = calculateTotalScore(filteredFactors);

        // 상위 3개 요소 추출
        List<ScoreSummary.TopFactor> topFactors = filteredFactors.stream()
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
     * 매물의 상세 점수 정보를 조회합니다.
     *
     * @param estateId 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 상세 점수 정보
     */
    @Transactional(readOnly = true)
    public ScoreDetails getScoreDetails(Long estateId, Long userId) {
        log.debug("Getting score details for estate: {} (userId: {})", estateId, userId != null ? userId : "guest");

        // 기본 점수 정보 조회
        List<ScoreDto> scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            log.debug("No score factors found for estate: {}", estateId);
            return new ScoreDetails(0.0, "점수 정보가 없습니다", List.of());
        }

        // 사용자 설정에 따라 필터링된 요소 목록 준비
        List<ScoreDto> filteredFactors = filterScoreFactorsByUserPreferences(scoreFactors, userId);
        if (filteredFactors.isEmpty()) {
            log.info("All score types are disabled by user: {}", userId);
            return new ScoreDetails(0.0, "모든 점수 유형이 비활성화되었습니다", List.of());
        }

        // 총점 계산
        double totalScore = calculateTotalScore(filteredFactors);

        // 모든 요소의 상세 정보 매핑
        List<ScoreDetails.ScoreFactor> factors = filteredFactors.stream()
            .map(factor -> new ScoreDetails.ScoreFactor(
                factor.getScoreTypeId(),
                factor.getScoreTypeName(),
                factor.getDescription(),
                factor.getNormalizedScore()
            ))
            .toList();

        String description = String.format("총 %d개 요소의 평균 점수입니다", factors.size());
        return new ScoreDetails(totalScore, description, factors);
    }

    /**
     * 사용자 설정에 따라 점수 요소를 필터링합니다.
     */
    private List<ScoreDto> filterScoreFactorsByUserPreferences(List<ScoreDto> scoreFactors, Long userId) {
        // 비인증 사용자는 모든 점수 요소 표시
        if (userId == null) {
            return scoreFactors;
        }

        // 사용자가 비활성화한 점수 유형 ID 목록 조회
        Set<Integer> disabledScoreTypeIds = getDisabledScoreTypeIds(userId);
        if (disabledScoreTypeIds.isEmpty()) {
            return scoreFactors;
        }

        // 비활성화된 점수 유형 필터링
        List<ScoreDto> filteredFactors = scoreFactors.stream()
            .filter(factor -> !disabledScoreTypeIds.contains(factor.getScoreTypeId()))
            .toList();

        log.debug("Filtered score factors: original={}, filtered={}, disabled={}",
            scoreFactors.size(), filteredFactors.size(), disabledScoreTypeIds.size());

        return filteredFactors;
    }

    /**
     * 사용자가 비활성화한 점수 유형 ID 목록을 조회합니다.
     */
    private Set<Integer> getDisabledScoreTypeIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        List<Integer> disabledScoreTypeIds = userDisabledScoreTypeRepository.findDisabledScoreTypeIdsByUserId(userId);
        log.debug("Disabled score types for user {}: {}", userId, disabledScoreTypeIds);
        return Set.copyOf(disabledScoreTypeIds);
    }

    /**
     * 사용자의 점수 유형 비활성화 설정을 추가합니다.
     */
    @Transactional
    public void disableScoreType(Long userId, Integer scoreTypeId) {
        log.info("Disabling score type: {} for user: {}", scoreTypeId, userId);
        UserDisabledScoreType disabledScoreType = UserDisabledScoreType.create(userId, scoreTypeId);
        userDisabledScoreTypeRepository.insert(disabledScoreType);
    }

    /**
     * 사용자의 점수 유형 비활성화 설정을 제거합니다.
     */
    @Transactional
    public void enableScoreType(Long userId, Integer scoreTypeId) {
        log.info("Enabling score type: {} for user: {}", scoreTypeId, userId);
        userDisabledScoreTypeRepository.delete(userId, scoreTypeId);
    }

    /**
     * 사용자의 점수 유형 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScoreTypeResponse> getAllScoreTypes(Long userId) {
        log.debug("Getting all score types for user: {}", userId != null ? userId : "guest");

        // 모든 점수 유형 조회
        List<Map<String, Object>> scoreTypes = apiScoreRepository.findAllScoreTypes();

        // 비활성화된 점수 유형 ID 조회
        Set<Integer> disabledScoreTypeIds = getDisabledScoreTypeIds(userId);

        // 사용자별 활성화 상태가 반영된 점수 유형 응답 생성
        return scoreTypes.stream()
            .map(scoreType -> {
                Integer id = (Integer) scoreType.get("id");
                String name = (String) scoreType.get("name");
                String description = (String) scoreType.get("description");

                // 비활성화 목록에 있으면 enabled=false, 없으면 enabled=true
                boolean enabled = !disabledScoreTypeIds.contains(id);

                return ScoreTypeResponse.of(id, name, description, enabled);
            })
            .toList();
    }

    /**
     * 점수 목록의 평균 점수를 계산합니다.
     */
    private double calculateTotalScore(List<ScoreDto> factors) {
        return factors.stream()
            .filter(factor -> factor.getNormalizedScore() != null)
            .mapToDouble(ScoreDto::getNormalizedScore)
            .average()
            .orElse(0.0);
    }
}