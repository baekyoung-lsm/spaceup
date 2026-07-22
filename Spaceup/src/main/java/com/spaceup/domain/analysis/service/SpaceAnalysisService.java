package com.spaceup.domain.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.analysis.dto.SpaceAnalysisResponse;
import com.spaceup.domain.analysis.dto.SpaceAnalysisResultRequest;
import com.spaceup.domain.analysis.entity.AnalysisStatus;
import com.spaceup.domain.analysis.entity.SpaceAnalysis;
import com.spaceup.domain.analysis.repository.SpaceAnalysisRepository;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.global.error.AnalysisNotFoundException;
import com.spaceup.global.error.RequestNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceAnalysisService {

	private final SpaceAnalysisRepository spaceAnalysisRepository;
	private final RequestRepository requestRepository;

	// ⭐ PDF "02 임대 정보 입력" 완료 직후 호출 지점. PENDING 상태로 분석 레코드를 먼저 만들어두고, ML 파이프라인에
	// 비동기로 분석을 맡긴 뒤 submitResult()로 콜백을 받는 구조입니다.
	@Transactional
	public Long requestAnalysis(Long requestId) {
		Request request = requestRepository.findById(requestId)
				.orElseThrow(() -> new RequestNotFoundException("존재하지 않는 의뢰입니다: " + requestId));

		SpaceAnalysis analysis = SpaceAnalysis.builder().request(request).status(AnalysisStatus.PENDING).build();
		spaceAnalysisRepository.save(analysis);
		return analysis.getId();
	}

	// ⭐ ML 파이프라인 콜백 또는 관리자 수동 보정
	// ⭐ [Figma 반영] 예상견적 범위 + ROI(예상 월세상승/회수기간) 필드를 함께 반영하도록 확장
	@Transactional
	public void submitResult(Long requestId, SpaceAnalysisResultRequest dto) {
		SpaceAnalysis analysis = findByRequestOrThrow(requestId);
		analysis.completeWith(dto.getRoomCount(), dto.getBathroomCount(), dto.getHasBalcony(), dto.getKitchenType(),
				dto.getSpaceScore(), dto.getConditionScore(), dto.getIssueTags(), dto.getEstimatedQuoteMin(),
				dto.getEstimatedQuoteMax(), dto.getExpectedRentIncreaseMin(), dto.getExpectedRentIncreaseMax(),
				dto.getPaybackPeriodMonthsMin(), dto.getPaybackPeriodMonthsMax());
	}

	@Transactional
	public void markFailed(Long requestId) {
		findByRequestOrThrow(requestId).fail();
	}

	// ⭐ domain/matching의 MatchingScoreCalculator 결과를 여기로 반영
	@Transactional
	public void updateMatchingScore(Long requestId, int score) {
		findByRequestOrThrow(requestId).updateMatchingScore(score);
	}

	// ⭐ 분석 레코드가 아직 없을 수도 있는 시점(예: 시공사 배정이 분석 완료보다 먼저 일어난 경우)에 안전하게 쓰는 버전.
	// 없으면 조용히 무시합니다 (RequestService.assignContractor()에서 사용).
	@Transactional
	public void updateMatchingScoreIfExists(Long requestId, int score) {
		spaceAnalysisRepository.findByRequestId(requestId).ifPresent(analysis -> analysis.updateMatchingScore(score));
	}

	public SpaceAnalysisResponse getByRequest(Long requestId) {
		return new SpaceAnalysisResponse(findByRequestOrThrow(requestId));
	}

	private SpaceAnalysis findByRequestOrThrow(Long requestId) {
		return spaceAnalysisRepository.findByRequestId(requestId)
				.orElseThrow(() -> new AnalysisNotFoundException("해당 의뢰의 분석 결과가 없습니다: " + requestId));
	}
}
