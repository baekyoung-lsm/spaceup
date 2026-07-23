package com.spaceup.domain.analysis.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.analysis.dto.AnalysisJobResponse;
import com.spaceup.domain.analysis.dto.AnalysisJobResultRequest;
import com.spaceup.domain.analysis.service.AnalysisJobService;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

// ⭐ PDF "공간 정보 확인" / "의뢰 상세 - AI분석" 화면. 결과 제출(submit)은 외부 ML 파이프라인의 콜백 용도라
// 실제 운영에서는 서버 간 인증(API Key 등)으로 별도 보호하는 게 좋습니다 (지금은 JWT 인증만 걸려 있음).
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisJobController {

	private final AnalysisJobService analysisJobService;

	// ⭐ PDF "02 임대 정보 입력" 완료 직후 - 분석을 PENDING 상태로 요청
	@PostMapping("/request/{requestId}")
	public ResponseEntity<ApiResponse<Long>> requestAnalysis(@PathVariable Long requestId) {
		Long analysisId = analysisJobService.requestAnalysis(requestId);
		return ResponseEntity.ok(ApiResponse.success("분석 요청이 접수되었습니다.", analysisId));
	}

	// ⭐ ML 파이프라인 콜백 (또는 관리자 수동 보정)
	@PostMapping("/request/{requestId}/result")
	public ResponseEntity<ApiResponse<Void>> submitResult(@PathVariable Long requestId,
			@Valid @RequestBody AnalysisJobResultRequest request) {
		analysisJobService.submitResult(requestId, request);
		return ResponseEntity.ok(ApiResponse.success("분석 결과가 반영되었습니다.", null));
	}

	@PostMapping("/request/{requestId}/fail")
	public ResponseEntity<ApiResponse<Void>> markFailed(@PathVariable Long requestId) {
		analysisJobService.markFailed(requestId);
		return ResponseEntity.ok(ApiResponse.success("분석 실패로 처리되었습니다.", null));
	}

	// ⭐ PDF "공간 정보 확인" 화면 조회
	@GetMapping("/request/{requestId}")
	public ResponseEntity<ApiResponse<AnalysisJobResponse>> getByRequest(@PathVariable Long requestId) {
		return ResponseEntity.ok(ApiResponse.success("분석 결과 조회 완료", analysisJobService.getByRequest(requestId)));
	}
}
