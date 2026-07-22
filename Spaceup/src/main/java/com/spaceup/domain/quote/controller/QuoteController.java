package com.spaceup.domain.quote.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.quote.dto.QuoteCreateRequest;
import com.spaceup.domain.quote.dto.QuoteExtendRequest;
import com.spaceup.domain.quote.dto.QuoteResponse;
import com.spaceup.domain.quote.dto.QuoteRevisionRequest;
import com.spaceup.domain.quote.service.QuoteService;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

	private final QuoteService quoteService;

	// ⭐ PDF "견적 작성 - 임시 저장" (시공사 로그인 기준)
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> createDraft(@Valid @RequestBody QuoteCreateRequest request,
			Authentication authentication) {
		Long contractorId = getMemberId(authentication);
		Long quoteId = quoteService.createDraft(contractorId, request);
		return ResponseEntity.ok(ApiResponse.success("견적이 임시 저장되었습니다.", quoteId));
	}

	@GetMapping("/{quoteId}")
	public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(@PathVariable Long quoteId) {
		return ResponseEntity.ok(ApiResponse.success("견적 조회 완료", quoteService.getQuote(quoteId)));
	}

	// ⭐ 하나의 의뢰에 달린 견적 이력 전체 (재견적 포함 - "v1/v2..." 버전은 이 목록의 순서/revisionCount로 구분)
	@GetMapping("/request/{requestId}")
	public ResponseEntity<ApiResponse<List<QuoteResponse>>> getQuotesByRequest(@PathVariable Long requestId) {
		return ResponseEntity.ok(ApiResponse.success("견적 목록 조회 완료", quoteService.getQuotesByRequest(requestId)));
	}

	// ⭐ PDF "견적 제안 보내기" 버튼
	@PostMapping("/{quoteId}/submit")
	public ResponseEntity<ApiResponse<Void>> submit(@PathVariable Long quoteId, Authentication authentication) {
		quoteService.submit(quoteId, getMemberId(authentication));
		return ResponseEntity.ok(ApiResponse.success("견적을 발송했습니다.", null));
	}

	// ⭐ 임대인이 마이페이지 등에서 견적을 최종 선택
	@PostMapping("/{quoteId}/accept")
	public ResponseEntity<ApiResponse<Void>> accept(@PathVariable Long quoteId, Authentication authentication) {
		quoteService.accept(quoteId, getMemberId(authentication));
		return ResponseEntity.ok(ApiResponse.success("견적을 선택했습니다.", null));
	}

	@PostMapping("/{quoteId}/reject")
	public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long quoteId, Authentication authentication) {
		quoteService.reject(quoteId, getMemberId(authentication));
		return ResponseEntity.ok(ApiResponse.success("견적을 거절했습니다.", null));
	}

	// ⭐ [Figma 반영] "유효기간 연장" 화면 - 작성한 시공사 본인만
	@PostMapping("/{quoteId}/extend")
	public ResponseEntity<ApiResponse<Void>> extend(@PathVariable Long quoteId,
			@Valid @RequestBody QuoteExtendRequest request, Authentication authentication) {
		quoteService.extendValidity(quoteId, getMemberId(authentication), request.getNewValidUntil());
		return ResponseEntity.ok(ApiResponse.success("견적 유효기간이 연장되었습니다.", null));
	}

	// ⭐ [Figma 반영] "보낸 견적 상세 - 수정 요청" 화면 - 해당 의뢰의 임대인 본인만
	@PostMapping("/{quoteId}/request-revision")
	public ResponseEntity<ApiResponse<Void>> requestRevision(@PathVariable Long quoteId,
			@Valid @RequestBody QuoteRevisionRequest request, Authentication authentication) {
		quoteService.requestRevision(quoteId, getMemberId(authentication), request.getNote());
		return ResponseEntity.ok(ApiResponse.success("수정 요청을 전달했습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
