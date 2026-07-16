package com.spaceup.domain.quote.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.global.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ⭐ PDF "견적 작성 / 견적 제안 작성" 화면에 대응합니다. 하나의 Request(의뢰)에는 여러 개의 Quote가 시간에 따라
 * 생길 수 있어 다대일로 연결하고(재견적 이력 관리), 견적 항목(철거/바닥/조명 등)은 QuoteItem으로 분리했습니다.
 */
@Entity
@Table(name = "quotes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Quote extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false)
	private Request request;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contractor_id", nullable = false)
	private Member contractor;

	@Column(name = "title", length = 100)
	private String title; // 견적 제목 (예: "역삼 오피스텔 리모델링 견적")

	@Column(name = "start_date")
	private String startDate; // 공사 시작 가능일

	@Column(name = "duration_days")
	private Integer durationDays; // 예상 공사 기간(일)

	@Column(name = "material_cost")
	private Long materialCost; // 자재비

	@Column(name = "labor_cost")
	private Long laborCost; // 인건비

	@Column(name = "vat")
	private Long vat; // 부가세

	@Column(name = "discount")
	private Long discount; // 할인 금액

	@Column(name = "total_amount")
	private Long totalAmount; // 최종 견적 금액

	@Column(name = "detail_content", length = 500)
	private String detailContent; // 견적 상세 내용 (작업범위/자재종류/추가비용 조건 등)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private QuoteStatus status;

	@Builder.Default
	@OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<QuoteItem> items = new ArrayList<>();

	public void addItem(QuoteItem item) {
		items.add(item);
		item.assignQuote(this);
	}

	public void submit() {
		this.status = QuoteStatus.SUBMITTED;
	}

	public void accept() {
		this.status = QuoteStatus.ACCEPTED;
	}

	public void reject() {
		this.status = QuoteStatus.REJECTED;
	}

	// ⭐ 자재비+인건비+부가세-할인 = 최종 견적. 항목/금액이 바뀔 때마다 서비스 레이어에서 호출해 재계산합니다.
	public void recalculateTotal() {
		long material = materialCost != null ? materialCost : 0;
		long labor = laborCost != null ? laborCost : 0;
		long vatAmount = vat != null ? vat : 0;
		long discountAmount = discount != null ? discount : 0;
		this.totalAmount = material + labor + vatAmount - discountAmount;
	}
}
