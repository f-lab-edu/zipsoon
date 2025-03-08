package com.zipsoon.api.application.estate;

import com.zipsoon.api.domain.user.UserDisabledScoreType;
import com.zipsoon.api.infrastructure.repository.estate.ApiScoreRepository;
import com.zipsoon.api.infrastructure.repository.user.UserDisabledScoreTypeRepository;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreDetailsResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreSummaryResponse;
import com.zipsoon.api.interfaces.api.estate.dto.ScoreTypeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
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
    public ScoreSummaryResponse getScoreSummary(Long estateId, Long userId) {
        log.debug("[SVC:IN] getScoreSummary(estateId={}, userId={})", estateId, userId != null ? userId : "guest");

        // 기본 점수 정보 조회
        var scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            log.debug("[SVC:RESULT] 매물 {}에 대한 점수 정보 없음", estateId);
            log.debug("[SVC:OUT] getScoreSummary() 완료 - 점수 정보 없음");
            return new ScoreSummaryResponse(0.0, List.of());
        }

        log.debug("[SVC:PARAM] 매물 {}의 점수 요소 {}개 검색됨", estateId, scoreFactors.size());

        // 사용자 설정에 따라 필터링된 요소 목록 준비
        var filteredFactors = filterScoreFactorsByUserPreferences(scoreFactors, userId);
        if (filteredFactors.isEmpty()) {
            log.info("[SVC:RESULT] 사용자 {}가 모든 점수 유형을 비활성화함", userId);
            log.debug("[SVC:OUT] getScoreSummary() 완료 - 활성화된 점수 유형 없음");
            return new ScoreSummaryResponse(0.0, List.of());
        }

        // 총점 계산
        var totalScore = calculateTotalScore(filteredFactors);

        // 상위 3개 요소 추출
        var topFactors = filteredFactors.stream()
            .sorted((f1, f2) -> Double.compare(f2.normalizedScore(), f1.normalizedScore()))
            .limit(3)
            .map(factor -> new ScoreSummaryResponse.TopFactorResponse(
                factor.scoreTypeId(),
                factor.scoreTypeName(),
                factor.normalizedScore()
            ))
            .toList();

        log.debug("[SVC:RESULT] 매물 {} 총점: {}, 상위 요소 {}개", estateId, totalScore, topFactors.size());
        log.debug("[SVC:OUT] getScoreSummary() 완료");
        return new ScoreSummaryResponse(totalScore, topFactors);
    }

    /**
     * 매물의 상세 점수 정보를 조회합니다.
     *
     * @param estateId 매물 ID
     * @param userId 사용자 ID (로그인한 경우에만 제공)
     * @return 상세 점수 정보
     */
    @Transactional(readOnly = true)
    public ScoreDetailsResponse getScoreDetails(Long estateId, Long userId) {
        log.debug("[SVC:IN] getScoreDetails(estateId={}, userId={})", estateId, userId != null ? userId : "guest");

        // 기본 점수 정보 조회
        var scoreFactors = apiScoreRepository.findScoresByEstateId(estateId);
        if (scoreFactors.isEmpty()) {
            log.debug("[SVC:RESULT] 매물 {}에 대한 점수 정보 없음", estateId);
            log.debug("[SVC:OUT] getScoreDetails() 완료 - 점수 정보 없음");
            return new ScoreDetailsResponse(0.0, "점수 정보가 없습니다", List.of());
        }

        log.debug("[SVC:PARAM] 매물 {}의 점수 요소 {}개 검색됨", estateId, scoreFactors.size());

        // 사용자 설정에 따라 필터링된 요소 목록 준비
        var filteredFactors = filterScoreFactorsByUserPreferences(scoreFactors, userId);
        if (filteredFactors.isEmpty()) {
            log.info("[SVC:RESULT] 사용자 {}가 모든 점수 유형을 비활성화함", userId);
            log.debug("[SVC:OUT] getScoreDetails() 완료 - 활성화된 점수 유형 없음");
            return new ScoreDetailsResponse(0.0, "모든 점수 유형이 비활성화되었습니다", List.of());
        }

        // 총점 계산
        var totalScore = calculateTotalScore(filteredFactors);

        // 모든 요소의 상세 정보 매핑
        var factors = filteredFactors.stream()
            .map(factor -> new ScoreDetailsResponse.ScoreFactorResponse(
                factor.scoreTypeId(),
                factor.scoreTypeName(),
                factor.description(),
                factor.normalizedScore()
            ))
            .toList();

        var description = String.format("총 %d개 요소의 평균 점수입니다", factors.size());
        
        log.debug("[SVC:RESULT] 매물 {} 총점: {}, 요소 {}개", estateId, totalScore, factors.size());
        log.debug("[SVC:OUT] getScoreDetails() 완료");
        
        return new ScoreDetailsResponse(totalScore, description, factors);
    }

    /**
     * 사용자 설정에 따라 점수 요소를 필터링합니다.
     */
    private List<ScoreResponse> filterScoreFactorsByUserPreferences(List<ScoreResponse> scoreFactors, Long userId) {
        // 비인증 사용자는 모든 점수 요소 표시
        if (userId == null) {
            log.debug("[SVC:PARAM] 비인증 사용자 - 모든 점수 요소 표시");
            return scoreFactors;
        }

        // 사용자가 비활성화한 점수 유형 ID 목록 조회
        Set<Integer> disabledScoreTypeIds = getDisabledScoreTypeIds(userId);
        if (disabledScoreTypeIds.isEmpty()) {
            log.debug("[SVC:PARAM] 사용자 {} - 비활성화된 점수 유형 없음", userId);
            return scoreFactors;
        }

        // 비활성화된 점수 유형 필터링
        List<ScoreResponse> filteredFactors = scoreFactors.stream()
            .filter(factor -> !disabledScoreTypeIds.contains(factor.scoreTypeId().intValue()))
            .toList();

        log.debug("[SVC:PARAM] 점수 요소 필터링: 원본={}, 필터링={}, 비활성화={}",
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

        var disabledScoreTypeIds = userDisabledScoreTypeRepository.findDisabledScoreTypeIdsByUserId(userId);
        log.debug("[SVC:PARAM] 사용자 {} 비활성화 점수 유형: {}", userId, disabledScoreTypeIds);
        return Set.copyOf(disabledScoreTypeIds);
    }

    /**
     * 사용자의 점수 유형 비활성화 설정을 추가합니다.
     */
    @Transactional
    public void disableScoreType(Long userId, Integer scoreTypeId) {
        log.debug("[SVC:IN] disableScoreType(userId={}, scoreTypeId={})", userId, scoreTypeId);
        
        // 이미 비활성화되어 있는지 확인
        boolean alreadyDisabled = userDisabledScoreTypeRepository.findDisabledScoreTypeIdsByUserId(userId)
            .contains(scoreTypeId);
            
        if (alreadyDisabled) {
            log.debug("[SVC:RESULT] 이미 비활성화되어 있음: 사용자={}, 점수유형={}", userId, scoreTypeId);
            log.debug("[SVC:OUT] disableScoreType() 완료 - 이미 비활성화됨");
            return;
        }
        
        var disabledScoreType = UserDisabledScoreType.of(userId, scoreTypeId);
        userDisabledScoreTypeRepository.save(disabledScoreType);
        
        log.info("[SVC:RESULT] 점수 유형 비활성화 완료: 사용자={}, 점수유형={}", userId, scoreTypeId);
        log.debug("[SVC:OUT] disableScoreType() 완료");
    }

    /**
     * 사용자의 점수 유형 비활성화 설정을 제거합니다.
     */
    @Transactional
    public void enableScoreType(Long userId, Integer scoreTypeId) {
        log.debug("[SVC:IN] enableScoreType(userId={}, scoreTypeId={})", userId, scoreTypeId);
        
        // 이미 활성화되어 있는지 확인
        boolean alreadyEnabled = !userDisabledScoreTypeRepository.findDisabledScoreTypeIdsByUserId(userId)
            .contains(scoreTypeId);
            
        if (alreadyEnabled) {
            log.debug("[SVC:RESULT] 이미 활성화되어 있음: 사용자={}, 점수유형={}", userId, scoreTypeId);
            log.debug("[SVC:OUT] enableScoreType() 완료 - 이미 활성화됨");
            return;
        }
        
        userDisabledScoreTypeRepository.delete(userId, scoreTypeId);
        
        log.info("[SVC:RESULT] 점수 유형 활성화 완료: 사용자={}, 점수유형={}", userId, scoreTypeId);
        log.debug("[SVC:OUT] enableScoreType() 완료");
    }

    /**
     * 사용자의 점수 유형 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScoreTypeResponse> getAllScoreTypes(Long userId) {
        log.debug("[SVC:IN] getAllScoreTypes(userId={})", userId != null ? userId : "guest");

        // 모든 점수 유형 조회
        var scoreTypes = apiScoreRepository.findAllScoreTypes();
        log.debug("[SVC:PARAM] 전체 점수 유형 {}개 조회됨", scoreTypes.size());

        // 비활성화된 점수 유형 ID 조회
        var disabledScoreTypeIds = getDisabledScoreTypeIds(userId);

        // 사용자별 활성화 상태가 반영된 점수 유형 응답 생성
        var result = scoreTypes.stream()
            .map(scoreType -> {
                var id = (Integer) scoreType.get("id");
                var name = (String) scoreType.get("name");
                var description = (String) scoreType.get("description");

                // 비활성화 목록에 있으면 enabled=false, 없으면 enabled=true
                var enabled = !disabledScoreTypeIds.contains(id);

                return ScoreTypeResponse.of(id, name, description, enabled);
            })
            .toList();
            
        log.info("[SVC:RESULT] 사용자 {}의 점수 유형 조회 완료: 총 {}개, 비활성화 {}개", 
                userId != null ? userId : "guest", 
                result.size(), 
                disabledScoreTypeIds.size());
                
        log.debug("[SVC:OUT] getAllScoreTypes() 완료");
        return result;
    }

    /**
     * 점수 목록의 평균 점수를 계산합니다.
     * 응용 계층에서 도메인 모델로 변환하여 도메인 계층의 로직을 활용합니다.
     */
    private double calculateTotalScore(List<ScoreResponse> factors) {
        // DTO에서 계산하는 대신 Domain 모델의 계산 로직 활용
        if (factors.isEmpty()) {
            return 0.0;
        }
        
        return factors.stream()
            .filter(factor -> factor.normalizedScore() != null)
            .mapToDouble(ScoreResponse::normalizedScore)
            .average()
            .orElse(0.0);
    }
}