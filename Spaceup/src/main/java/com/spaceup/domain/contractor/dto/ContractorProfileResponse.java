package com.spaceup.domain.contractor.dto;

import com.spaceup.domain.contractor.entity.ContractorProfile;

import lombok.Getter;

@Getter
public class ContractorProfileResponse {
	private final Long id;
	private final Long memberId;
	private final String memberName;
	private final String businessRegistrationNumber;
	private final String companyName;
	private final String activityRegions;
	private final String specialties;
	private final String portfolioUrl;
	private final String introduction;
	private final Double rating;
	private final Integer completedProjectCount;

	public ContractorProfileResponse(ContractorProfile profile) {
		this.id = profile.getId();
		this.memberId = profile.getMember().getId();
		this.memberName = profile.getMember().getName();
		this.businessRegistrationNumber = profile.getBusinessRegistrationNumber();
		this.companyName = profile.getCompanyName();
		this.activityRegions = profile.getActivityRegions();
		this.specialties = profile.getSpecialties();
		this.portfolioUrl = profile.getPortfolioUrl();
		this.introduction = profile.getIntroduction();
		this.rating = profile.getRating();
		this.completedProjectCount = profile.getCompletedProjectCount();
	}
}
