package com.spaceup.domain.request.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ⭐ PDF "공간 정보 확인" / "의뢰 상세 - 요약" 화면의 AI 분석 결과를 값 객체로 분리했습니다. Request 엔티티에
 * @Embedded로 포함되며, 외부 AI 분석 파이프라인(ML 서버)의 응답을 이 구조로 매핑해 저장하면 됩니다. 실제 계산 로직은 이
 * 서버가 아니라 ML 파이프라인 쪽 책임이라, 여기서는 "결과를 담는 그릇" 역할만 합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class SpaceAnalysis {

	@Column(name = "room_count")
	private Integer roomCount; // 방 개수

	@Column(name = "bathroom_count")
	private Integer bathroomCount; // 욕실 개수

	@Column(name = "has_balcony")
	private Boolean hasBalcony; // 발코니 유무

	@Column(name = "kitchen_type", length = 20)
	private String kitchenType; // 주방 형태 (일체형/분리형)

	@Column(name = "space_score")
	private Integer spaceScore; // 공간 효율 점수 (0~100)

	@Column(name = "condition_score")
	private Integer conditionScore; // 컨디션/노후도 점수 (0~100)

	@Column(name = "issue_tags", length = 500)
	private String issueTags; // AI 분석 태그 (예: "조명 어두움,바닥 노후화,수납 부족" - 콤마 구분 저장)

	@Column(name = "matching_score")
	private Integer matchingScore; // 시공사 매칭 점수 (0~100) - domain/matching 계산 결과를 저장
}
