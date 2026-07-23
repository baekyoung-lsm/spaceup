package com.spaceup.domain.analysis.dto;

import com.spaceup.domain.analysis.entity.AnalysisJob;
import com.spaceup.domain.analysis.entity.AnalysisStatus;

import lombok.Getter;

@Getter
public class AnalysisJobResponse {
	private final Long id;
	private final Long requestId;
	private final AnalysisStatus status;
	private final Integer roomCount;
	private final Integer bathroomCount;
	private final Boolean hasBalcony;
	private final String kitchenType;
	private final Integer spaceScore;
	private final Integer conditionScore;
	private final String issueTags;
	private final Integer matchingScore;
	private final Long estimatedQuoteMin;
	private final Long estimatedQuoteMax;
	private final Long expectedRentIncreaseMin;
	private final Long expectedRentIncreaseMax;
	private final Integer paybackPeriodMonthsMin;
	private final Integer paybackPeriodMonthsMax;

	public AnalysisJobResponse(AnalysisJob analysis) {
		this.id = analysis.getId();
		this.requestId = analysis.getRequest().getId();
		this.status = analysis.getStatus();
		this.roomCount = analysis.getRoomCount();
		this.bathroomCount = analysis.getBathroomCount();
		this.hasBalcony = analysis.getHasBalcony();
		this.kitchenType = analysis.getKitchenType();
		this.spaceScore = analysis.getSpaceScore();
		this.conditionScore = analysis.getConditionScore();
		this.issueTags = analysis.getIssueTags();
		this.matchingScore = analysis.getMatchingScore();
		this.estimatedQuoteMin = analysis.getEstimatedQuoteMin();
		this.estimatedQuoteMax = analysis.getEstimatedQuoteMax();
		this.expectedRentIncreaseMin = analysis.getExpectedRentIncreaseMin();
		this.expectedRentIncreaseMax = analysis.getExpectedRentIncreaseMax();
		this.paybackPeriodMonthsMin = analysis.getPaybackPeriodMonthsMin();
		this.paybackPeriodMonthsMax = analysis.getPaybackPeriodMonthsMax();
	}
}
