package com.spaceup.domain.analysis.dto;

import com.spaceup.domain.analysis.entity.AnalysisStatus;
import com.spaceup.domain.analysis.entity.SpaceAnalysis;

import lombok.Getter;

@Getter
public class SpaceAnalysisResponse {
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

	public SpaceAnalysisResponse(SpaceAnalysis analysis) {
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
	}
}
