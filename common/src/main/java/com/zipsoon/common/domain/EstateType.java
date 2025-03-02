package com.zipsoon.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EstateType {
	APT("아파트"),
	OPST("오피스텔"),
	VL("빌라"),
	ABYG("아파트분양권"),
	OBYG("오피스텔분양권"),
	JGC("재건축"),
	JWJT("전원주택"),
	DDDGG("단독/다가구"),
	SGJT("상가주택"),
	HOJT("한옥주택"),
	JGB("재개발"),
	OR("원룸"),
	SG("상가"),
	SMS("사무실"),
	GJCG("공장/창고"),
	GM("건물"),
	TJ("토지"),
	APTHGJ("지식산업센터");

	private final String koreanName;

	EstateType(String koreanName) {
		this.koreanName = koreanName;
	}

	@JsonValue
	public String getKoreanName() {
		return koreanName;
	}

	public static EstateType of(String koreanName) {
		for (EstateType estateType : values()) {
			if (estateType.koreanName.equals(koreanName)) {
				return estateType;
			}
		}
		return null;
	}
}