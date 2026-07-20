package com.spaceup.domain.analysis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ⭐ 외부 ML 파이프라인이 분석을 끝내고 결과를 서버로 콜백할 때 보내는 요청 (또는 관리자가 수동 보정할 때도 사용)
@Getter
@Setter
@NoArgsConstructor
public class SpaceAnalysisResultRequest {

	private Integer roomCount;
	private Integer bathroomCount;
	private Boolean hasBalcony;
	private String kitchenType;
	private Integer spaceScore;
	private Integer conditionScore;
	private String issueTags; // "조명 어두움,바닥 노후화" 콤마 구분
}
