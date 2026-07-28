package com.spaceup.domain.matching.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.spaceup.domain.contractor.entity.ContractorProfile;
import com.spaceup.domain.contractor.repository.ContractorProfileRepository;
import com.spaceup.domain.request.entity.QuoteRequest;

import lombok.RequiredArgsConstructor;

/**
 * ⭐ [시공사 추천 점수 구성] 반영 - 리뷰(40) + 예상 견적 적합도(35) + 일정 적합도(25) = 100점.
 * 기존의 "활동지역+전문분야" 기반 규칙은 이 공식으로 완전히 대체합니다.
 */
@Service
@RequiredArgsConstructor
public class RuleBasedMatchingScoreCalculator implements MatchingScoreCalculator {

	private static final int MAX_RATING_SCORE = 32;
	private static final int MAX_REVIEW_SCORE = 40;
	private static final int MAX_PRICE_SCORE = 35;
	private static final int MAX_SCHEDULE_SCORE = 25;
	private static final int MAX_TOTAL_SCORE = 100;

	private final ContractorProfileRepository contractorProfileRepository;

	@Override
	public int calculate(QuoteRequest request, Long contractorId) {
		// ⭐ 프로필이 아직 없으면(견적범위/가능일 등 입력 전) 점수를 매길 근거가 없어 0점입니다.
		return contractorProfileRepository.findByMemberId(contractorId).map(profile -> score(request, profile))
				.orElse(0);
	}

	private int score(QuoteRequest request, ContractorProfile profile) {
		Long userEstimate = request.getBudgetMax() != null ? request.getBudgetMax() : request.getBudget();
		LocalDate desiredDate = parseDate(request.getDesiredDate());

		int reviewScore = reviewScore(profile.getRating(), profile.getReviewCount());
		int priceScore = priceScore(userEstimate, profile.getEstimateMin(), profile.getEstimateMax());
		int scheduleScore = scheduleScore(desiredDate, profile.getAvailableFromDate());

		return Math.min(MAX_TOTAL_SCORE, reviewScore + priceScore + scheduleScore);
	}

	// ⭐ 1. 리뷰 점수 - 40점 (평점 최대 32점 + 리뷰개수 최대 8점)
	private int reviewScore(Double rating, Integer reviewCount) {
		double ratingScore = rating != null ? Math.min(MAX_RATING_SCORE, rating / 5.0 * MAX_RATING_SCORE) : 0;
		int countScore = reviewCountScore(reviewCount != null ? reviewCount : 0);
		return (int) Math.min(MAX_REVIEW_SCORE, Math.round(ratingScore + countScore));
	}

	private int reviewCountScore(int reviewCount) {
		if (reviewCount >= 100) {
			return 8;
		} else if (reviewCount >= 50) {
			return 6;
		} else if (reviewCount >= 20) {
			return 4;
		} else if (reviewCount >= 10) {
			return 2;
		} else if (reviewCount >= 1) {
			return 1;
		}
		return 0;
	}

	// ⭐ 2. 예상 견적 적합도 - 35점. 사용자 예상 견적이 업체 견적 범위(estimateMin~Max) 안에 들면 만점,
	// 벗어난 정도(차이율)에 따라 감점합니다.
	private int priceScore(Long userEstimate, Long contractorMin, Long contractorMax) {
		if (userEstimate == null || userEstimate <= 0 || contractorMin == null || contractorMax == null) {
			return 0;
		}
		if (userEstimate >= contractorMin && userEstimate <= contractorMax) {
			return MAX_PRICE_SCORE;
		}

		double diffPercent;
		if (userEstimate < contractorMin) {
			diffPercent = (contractorMin - userEstimate) / (double) userEstimate * 100;
		} else {
			diffPercent = (userEstimate - contractorMax) / (double) userEstimate * 100;
		}

		if (diffPercent <= 10) {
			return 28;
		} else if (diffPercent <= 20) {
			return 20;
		} else if (diffPercent <= 30) {
			return 10;
		}
		return 0;
	}

	// ⭐ 3. 일정 적합도 - 25점. 희망 공사 시작일 대비 업체의 가장 빠른 가능일이 얼마나 늦는지로 채점합니다.
	private int scheduleScore(LocalDate desiredDate, LocalDate availableDate) {
		if (desiredDate == null || availableDate == null) {
			return 0;
		}
		long daysLate = ChronoUnit.DAYS.between(desiredDate, availableDate);
		if (daysLate <= 0) {
			return MAX_SCHEDULE_SCORE;
		} else if (daysLate <= 7) {
			return 20;
		} else if (daysLate <= 14) {
			return 15;
		} else if (daysLate <= 30) {
			return 8;
		}
		return 0;
	}

	// ⭐ QuoteRequest.desiredDate는 "yyyy-MM-dd" 형식의 자유 입력 문자열이라 파싱 실패 시 점수 계산에서 제외합니다.
	private LocalDate parseDate(String desiredDate) {
		if (desiredDate == null) {
			return null;
		}
		try {
			return LocalDate.parse(desiredDate);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
