package com.spaceup.domain.request.dto;

import java.time.LocalDateTime;

import com.spaceup.domain.request.entity.RejectReason;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.entity.RequestStatus;

import lombok.Getter;

@Getter
public class RequestResponse {
	private final Long id;
	private final String requestCode;
	private final Long landlordId;
	private final String landlordName;
	private final Long contractorId;
	private final String region;
	private final String propertyType;
	private final Double areaM2;
	private final Long budget;
	private final Long budgetMin;
	private final Long budgetMax;
	private final String desiredDate;
	private final String requestedItems;
	private final RequestStatus status;
	private final RejectReason rejectReason;
	private final String rejectReasonDetail;
	private final LocalDateTime lastActivityAt;
	// ⭐ [Figma 반영] "의뢰 목록" 카드에 매칭 점수가 바로 보여서, 분석 API를 따로 안 타도 되게 여기에도 노출합니다.
	// 값은 RequestService가 SpaceAnalysisRepository에서 조회해 주입합니다(분석 전이면 null).
	private final Integer matchingScore;
	private final LocalDateTime createdAt;

	public RequestResponse(Request request) {
		this(request, null);
	}

	public RequestResponse(Request request, Integer matchingScore) {
		this.id = request.getId();
		this.requestCode = request.getRequestCode();
		this.landlordId = request.getLandlord().getId();
		this.landlordName = request.getLandlord().getName();
		this.contractorId = request.getContractor() != null ? request.getContractor().getId() : null;
		this.region = request.getRegion();
		this.propertyType = request.getPropertyType();
		this.areaM2 = request.getAreaM2();
		this.budget = request.getBudget();
		this.budgetMin = request.getBudgetMin();
		this.budgetMax = request.getBudgetMax();
		this.desiredDate = request.getDesiredDate();
		this.requestedItems = request.getRequestedItems();
		this.status = request.getStatus();
		this.rejectReason = request.getRejectReason();
		this.rejectReasonDetail = request.getRejectReasonDetail();
		this.lastActivityAt = request.getLastActivityAt();
		this.matchingScore = matchingScore;
		this.createdAt = request.getCreatedAt();
	}
}
