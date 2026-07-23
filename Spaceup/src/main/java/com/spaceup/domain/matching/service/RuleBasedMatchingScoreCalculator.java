package com.spaceup.domain.matching.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.spaceup.domain.contractor.entity.ContractorProfile;
import com.spaceup.domain.contractor.repository.ContractorProfileRepository;
import com.spaceup.domain.request.entity.QuoteRequest;

import lombok.RequiredArgsConstructor;

/**
 * ⭐ 규칙 기반 기본 구현체입니다. 외부 ML 매칭 서버가 생기면 같은 인터페이스로 AiMatchingScoreCalculator를
 * 새로 만들어 @Primary만 붙이면 이 구현체를 안 지우고도 교체할 수 있습니다.
 *
 * 점수 구성 (총 100점): 활동지역 일치 40점 + 전문분야 일치도(요청 항목과 겹치는 비율) 40점 + 기본 참여점수 20점
 */
@Service
@RequiredArgsConstructor
public class RuleBasedMatchingScoreCalculator implements MatchingScoreCalculator {

	private static final int REGION_MATCH_SCORE = 40;
	private static final int SPECIALTY_MATCH_MAX_SCORE = 40;
	private static final int BASE_SCORE = 20; // 활동중이라는 사실 자체에 부여하는 기본 점수

	private final ContractorProfileRepository contractorProfileRepository;

	@Override
	public int calculate(QuoteRequest request, Long contractorId) {
		return contractorProfileRepository.findByMemberId(contractorId).map(profile -> score(request, profile))
				.orElse(BASE_SCORE); // 프로필이 아직 없으면(온보딩 전) 기본 점수만 부여
	}

	private int score(QuoteRequest request, ContractorProfile profile) {
		int score = BASE_SCORE;
		score += regionScore(request.getProperty().getRegion(), profile.getActivityRegions());
		score += specialtyScore(request.getRequestedItems(), profile.getSpecialties());
		return Math.min(score, 100);
	}

	private int regionScore(String requestRegion, String activityRegions) {
		if (requestRegion == null || activityRegions == null) {
			return 0;
		}
		Set<String> regions = toSet(activityRegions);
		return regions.contains(requestRegion.trim()) ? REGION_MATCH_SCORE : 0;
	}

	private int specialtyScore(String requestedItems, String specialties) {
		if (requestedItems == null || specialties == null) {
			return 0;
		}
		Set<String> requested = toSet(requestedItems);
		Set<String> owned = toSet(specialties);
		if (requested.isEmpty()) {
			return 0;
		}
		long matched = requested.stream().filter(owned::contains).count();
		return (int) Math.round(SPECIALTY_MATCH_MAX_SCORE * ((double) matched / requested.size()));
	}

	// "광주 북구,광주 서구" 같은 콤마 구분 문자열을 Set으로 변환 (공백 트림)
	private Set<String> toSet(String commaSeparated) {
		return new HashSet<>(Arrays.asList(commaSeparated.split("\\s*,\\s*")));
	}
}
