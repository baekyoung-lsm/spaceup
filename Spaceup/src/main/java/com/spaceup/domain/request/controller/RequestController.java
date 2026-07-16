package com.spaceup.domain.request.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.request.dto.RequestCreateRequest;
import com.spaceup.domain.request.dto.RequestResponse;
import com.spaceup.domain.request.service.RequestService;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

	private final RequestService requestService;

	// ⭐ PDF "02 임대 정보 입력" 완료 버튼 → 의뢰 생성 (로그인한 임대인 본인 명의로 생성)
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> create(@Valid @RequestBody RequestCreateRequest request,
			Authentication authentication) {
		Long landlordId = getMemberId(authentication);
		Long requestId = requestService.createRequest(landlordId, request);
		return ResponseEntity.ok(ApiResponse.success("의뢰가 등록되었습니다.", requestId));
	}

	// ⭐ PDF "의뢰 상세" 화면 조회
	@GetMapping("/{requestId}")
	public ResponseEntity<ApiResponse<RequestResponse>> getRequest(@PathVariable Long requestId) {
		return ResponseEntity.ok(ApiResponse.success("의뢰 상세 조회 완료", requestService.getRequest(requestId)));
	}

	// ⭐ PDF "의뢰 목록" 화면 (시공사 로그인 기준 - 본인에게 배정된 의뢰만 조회)
	@GetMapping("/contractor/me")
	public ResponseEntity<ApiResponse<List<RequestResponse>>> getMyRequestsAsContractor(
			Authentication authentication) {
		Long contractorId = getMemberId(authentication);
		return ResponseEntity.ok(ApiResponse.success("의뢰 목록 조회 완료", requestService.getRequestsForContractor(contractorId)));
	}

	// ⭐ PDF "마이페이지 - 견적 요청 내역" 화면 (임대인 로그인 기준)
	@GetMapping("/landlord/me")
	public ResponseEntity<ApiResponse<List<RequestResponse>>> getMyRequestsAsLandlord(Authentication authentication) {
		Long landlordId = getMemberId(authentication);
		return ResponseEntity.ok(ApiResponse.success("견적 요청 내역 조회 완료", requestService.getRequestsForLandlord(landlordId)));
	}

	// ⭐ PDF "08 견적 요청하기" 버튼 → 특정 시공사에게 의뢰를 배정
	@PostMapping("/{requestId}/assign/{contractorId}")
	public ResponseEntity<ApiResponse<Void>> assignContractor(@PathVariable Long requestId,
			@PathVariable Long contractorId) {
		requestService.assignContractor(requestId, contractorId);
		return ResponseEntity.ok(ApiResponse.success("시공사에게 견적 요청이 전달되었습니다.", null));
	}

	// ⭐ PDF "의뢰 상세" 화면의 "의뢰 승인" 버튼 (시공사 액션)
	@PostMapping("/{requestId}/approve")
	public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long requestId) {
		requestService.approve(requestId);
		return ResponseEntity.ok(ApiResponse.success("의뢰를 승인했습니다.", null));
	}

	// ⭐ PDF "의뢰 상세" 화면의 "의뢰 거절" 버튼 (시공사 액션)
	@PostMapping("/{requestId}/reject")
	public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long requestId) {
		requestService.reject(requestId);
		return ResponseEntity.ok(ApiResponse.success("의뢰를 거절했습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
