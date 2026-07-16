package com.spaceup.domain.request.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.global.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ⭐ PDF "임대 정보 입력 ~ 의뢰 상세" 화면들의 핵심 엔티티입니다. 임대인이 입력한 매물/희망 정보 + AI 분석 결과
 * (SpaceAnalysis)를 함께 들고 있고, 시공사가 이 Request를 확인해서 Quote(견적)를 붙이는 구조입니다.
 *
 * 개발 순서상 이 도메인이 회원 다음으로 가장 먼저 필요합니다. Request가 없으면 Quote/Matching/Notification/Schedule
 * 어느 것도 만들 근거가 없기 때문입니다.
 */
@Entity
@Table(name = "requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Request extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ⭐ 화면에 보이는 "REQ-260715-012" 같은 사람이 읽는 코드. DB 내부 PK(id)와 분리해서 운영합니다.
	@Column(name = "request_code", nullable = false, unique = true, length = 30)
	private String requestCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "landlord_id", nullable = false)
	private Member landlord; // 의뢰를 등록한 임대인

	// ⭐ 특정 시공사를 지정해서 견적을 요청한 경우(PDF 08 견적 요청 화면). 아직 매칭 전이면 null일 수 있습니다.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contractor_id")
	private Member contractor;

	@Column(nullable = false, length = 50)
	private String region; // 지역 (예: 광주 북구)

	@Column(name = "property_type", nullable = false, length = 20)
	private String propertyType; // 주택 유형 (아파트/오피스텔/빌라 등)

	@Column(name = "area_m2", nullable = false)
	private Double areaM2; // 전용 면적(㎡)

	@Column(name = "deposit")
	private Long deposit; // 보증금(원)

	@Column(name = "monthly_rent")
	private Long monthlyRent; // 현재 월세(원)

	@Column(name = "target_rent")
	private Long targetRent; // 목표 월세(원)

	@Column(name = "budget")
	private Long budget; // 리모델링 예산(원)

	@Column(name = "desired_date")
	private String desiredDate; // 희망 시공/입주 일정 (yyyy-MM-dd)

	@Column(name = "requested_items", length = 200)
	private String requestedItems; // 요청 항목 (예: "도배,장판,조명" - 콤마 구분)

	@Embedded
	private SpaceAnalysis spaceAnalysis; // AI 분석 결과 (방/욕실/이슈태그/매칭점수 등)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RequestStatus status;

	// ===== 상태 전이 메서드 (도메인 로직은 서비스가 아니라 엔티티가 책임지도록) =====

	public void assignContractor(Member contractor) {
		this.contractor = contractor;
		this.status = RequestStatus.REVIEWING;
	}

	public void approve() {
		this.status = RequestStatus.QUOTE_REQUESTED;
	}

	public void reject() {
		this.status = RequestStatus.REJECTED;
	}

	public void startProgress() {
		this.status = RequestStatus.IN_PROGRESS;
	}

	public void complete() {
		this.status = RequestStatus.COMPLETED;
	}

	public void applySpaceAnalysis(SpaceAnalysis spaceAnalysis) {
		this.spaceAnalysis = spaceAnalysis;
	}
}
