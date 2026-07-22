package com.spaceup.domain.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.spaceup.domain.request.entity.Request;
import com.spaceup.global.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ⭐ PDF "공간 정보 확인" / "의뢰 상세 - AI분석" 화면. 기존엔 Request에 @Embedded로 붙어있었지만, 분석은
 * (1) 외부 ML 파이프라인이 비동기로 채워주고 (2) 재분석 요청이 생길 수 있고 (3) 상태(PENDING/FAILED) 관리가
 * 필요해서 독립 엔티티로 분리했습니다. Request : SpaceAnalysis = 1 : 1 이지만, FK는 이쪽(SpaceAnalysis)이
 * 들고 있어서 "아직 분석 전"인 Request도 자유롭게 만들 수 있습니다.
 */
@Entity
@Table(name = "space_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SpaceAnalysis extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false, unique = true)
	private Request request;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AnalysisStatus status;

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
	private String issueTags; // AI 분석 태그 (콤마 구분, 예: "조명 어두움,바닥 노후화")

	@Column(name = "matching_score")
	private Integer matchingScore; // 시공사 매칭 점수 (0~100) - domain/matching 계산 결과 저장

	// ⭐ [Figma 반영] "의뢰 상세 - AI분석" 탭의 "사용자가 받은 예상 견적" 범위 (예: 450만~550만원)
	@Column(name = "estimated_quote_min")
	private Long estimatedQuoteMin;

	@Column(name = "estimated_quote_max")
	private Long estimatedQuoteMax;

	// ⭐ [Figma 반영] "ROI 요약" 카드. 현재 월세는 Request.monthlyRent를 그대로 참조해서 쓰고(중복 저장 안 함),
	// 여기서는 AI가 계산한 예상 상승분/회수기간만 보관합니다.
	@Column(name = "expected_rent_increase_min")
	private Long expectedRentIncreaseMin;

	@Column(name = "expected_rent_increase_max")
	private Long expectedRentIncreaseMax;

	@Column(name = "payback_period_months_min")
	private Integer paybackPeriodMonthsMin;

	@Column(name = "payback_period_months_max")
	private Integer paybackPeriodMonthsMax;

	// ⭐ ML 파이프라인 콜백이 이 메서드로 결과를 채웁니다.
	public void completeWith(Integer roomCount, Integer bathroomCount, Boolean hasBalcony, String kitchenType,
			Integer spaceScore, Integer conditionScore, String issueTags, Long estimatedQuoteMin,
			Long estimatedQuoteMax, Long expectedRentIncreaseMin, Long expectedRentIncreaseMax,
			Integer paybackPeriodMonthsMin, Integer paybackPeriodMonthsMax) {
		this.roomCount = roomCount;
		this.bathroomCount = bathroomCount;
		this.hasBalcony = hasBalcony;
		this.kitchenType = kitchenType;
		this.spaceScore = spaceScore;
		this.conditionScore = conditionScore;
		this.issueTags = issueTags;
		this.estimatedQuoteMin = estimatedQuoteMin;
		this.estimatedQuoteMax = estimatedQuoteMax;
		this.expectedRentIncreaseMin = expectedRentIncreaseMin;
		this.expectedRentIncreaseMax = expectedRentIncreaseMax;
		this.paybackPeriodMonthsMin = paybackPeriodMonthsMin;
		this.paybackPeriodMonthsMax = paybackPeriodMonthsMax;
		this.status = AnalysisStatus.COMPLETED;
	}

	public void fail() {
		this.status = AnalysisStatus.FAILED;
	}

	public void updateMatchingScore(int matchingScore) {
		this.matchingScore = matchingScore;
	}
}
