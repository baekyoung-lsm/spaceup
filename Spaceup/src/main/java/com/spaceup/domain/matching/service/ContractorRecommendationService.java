package com.spaceup.domain.matching.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.contractor.entity.ContractorProfile;
import com.spaceup.domain.contractor.repository.ContractorProfileRepository;
import com.spaceup.domain.matching.dto.RecommendedContractorResponse;
import com.spaceup.domain.request.entity.QuoteRequest;
import com.spaceup.domain.request.repository.QuoteRequestRepository;
import com.spaceup.global.error.RequestNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * ⭐ [시공사 추천 점수 구성] "백엔드 처리 순서" 5~8단계 - 상담 가능한(availableForConsult=true) 시공사 전원에
 * 대해 MatchingScoreCalculator로 점수를 매기고, 점수 높은 순으로 정렬해 상위 3개만 추립니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractorRecommendationService {

	private static final int TOP_N = 3;

	private final QuoteRequestRepository quoteRequestRepository;
	private final ContractorProfileRepository contractorProfileRepository;
	private final MatchingScoreCalculator matchingScoreCalculator;

	public List<RecommendedContractorResponse> recommend(Long requestId) {
		QuoteRequest request = quoteRequestRepository.findById(requestId)
				.orElseThrow(() -> new RequestNotFoundException("존재하지 않는 의뢰입니다: " + requestId));

		return contractorProfileRepository.findByAvailableForConsultTrue().stream()
				.map(profile -> toResponse(profile,
						matchingScoreCalculator.calculate(request, profile.getMember().getId())))
				.sorted(Comparator.comparingInt(RecommendedContractorResponse::matchScore).reversed())
				.limit(TOP_N)
				.collect(Collectors.toList());
	}

	private RecommendedContractorResponse toResponse(ContractorProfile profile, int matchScore) {
		return new RecommendedContractorResponse(profile.getMember().getId(), profile.getCompanyName(),
				profile.getRating(), profile.getReviewCount(), profile.getEstimateMin(), profile.getEstimateMax(),
				profile.getAvailableFromDate(), matchScore);
	}
}
