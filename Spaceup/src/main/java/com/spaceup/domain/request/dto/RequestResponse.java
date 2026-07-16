package com.spaceup.domain.request.dto;

import java.time.LocalDateTime;

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
	private final String desiredDate;
	private final String requestedItems;
	private final Integer matchingScore; // spaceAnalysis에서 꺼낸 매칭 점수 (없으면 null)
	private final RequestStatus status;
	private final LocalDateTime createdAt;

	public RequestResponse(Request request) {
		this.id = request.getId();
		this.requestCode = request.getRequestCode();
		this.landlordId = request.getLandlord().getId();
		this.landlordName = request.getLandlord().getName();
		this.contractorId = request.getContractor() != null ? request.getContractor().getId() : null;
		this.region = request.getRegion();
		this.propertyType = request.getPropertyType();
		this.areaM2 = request.getAreaM2();
		this.budget = request.getBudget();
		this.desiredDate = request.getDesiredDate();
		this.requestedItems = request.getRequestedItems();
		this.matchingScore = request.getSpaceAnalysis() != null ? request.getSpaceAnalysis().getMatchingScore()
				: null;
		this.status = request.getStatus();
		this.createdAt = request.getCreatedAt();
	}
}
