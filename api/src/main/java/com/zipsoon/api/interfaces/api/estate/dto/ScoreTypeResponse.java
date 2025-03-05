package com.zipsoon.api.interfaces.api.estate.dto;

/**
 * 점수 유형 응답 DTO
 * 사용자가 활성화/비활성화할 수 있는 점수 유형 정보를 담고 있습니다.
 */
public record ScoreTypeResponse(
    Integer id,              // 점수 유형 ID
    String name,             // 점수 유형 이름
    String description,      // 점수 유형 설명
    boolean enabled          // 활성화 여부 (true: 활성화, false: 비활성화)
) {
    /**
     * 기본 활성화 상태의 점수 유형 응답 생성
     * 
     * @param id 점수 유형 ID
     * @param name 점수 유형 이름
     * @param description 점수 유형 설명
     * @return 점수 유형 응답
     */
    public static ScoreTypeResponse of(Integer id, String name, String description) {
        return new ScoreTypeResponse(id, name, description, true);
    }
    
    /**
     * 활성화 상태를 지정한 점수 유형 응답 생성
     * 
     * @param id 점수 유형 ID
     * @param name 점수 유형 이름
     * @param description 점수 유형 설명
     * @param enabled 활성화 여부
     * @return 점수 유형 응답
     */
    public static ScoreTypeResponse of(Integer id, String name, String description, boolean enabled) {
        return new ScoreTypeResponse(id, name, description, enabled);
    }
}