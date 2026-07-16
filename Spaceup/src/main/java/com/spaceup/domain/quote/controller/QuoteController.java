package com.spaceup.domain.quote.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.quote.dto.QuoteCreateRequest;
import com.spaceup.domain.quote.dto.QuoteResponse;
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

	// ⭐ 하나의 의뢰에 달린 견적 이력 전체 (재견적 포함)
	@GetMapping("/request/{requestId}")
	public ResponseEntity<ApiResponse<List<QuoteResponse>>> getQuotesByRequest(@PathVariable Long requestId) {
		return ResponseEntity.ok(ApiResponse.success("견적 목록 조회 완료", quoteService.getQuotesByRequest(requestId)));
	}

	// ⭐ PDF "견적 제안 보내기" 버튼
	@PostMapping("/{quoteId}/submit")
	public ResponseEntity<ApiResponse<Void>> submit(@PathVariable Long quoteId) {
		quoteService.submit(quoteId);
		return ResponseEntity.ok(ApiResponse.success("견적을 발송했습니다.", null));
	}

	// ⭐ 임대인이 마이페이지 등에서 견적을 최종 선택
	@PostMapping("/{quoteId}/accept")
	public ResponseEntity<ApiResponse<Void>> accept(@PathVariable Long quoteId) {
		quoteService.accept(quoteId);
		return ResponseEntity.ok(ApiResponse.success("견적을 선택했습니다.", null));
	}

	@PostMapping("/{quoteId}/reject")
	public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long quoteId) {
		quoteService.reject(quoteId);
		return ResponseEntity.ok(ApiResponse.success("견적을 거절했습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
