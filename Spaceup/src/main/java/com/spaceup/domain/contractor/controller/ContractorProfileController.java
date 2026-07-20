package com.spaceup.domain.contractor.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.contractor.dto.ContractorProfileResponse;
import com.spaceup.domain.contractor.dto.ContractorProfileUpdateRequest;
import com.spaceup.domain.contractor.service.ContractorProfileService;
import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

// ⭐ PDF "마이페이지/설정" 화면(시공사) - 사업자정보/활동지역/전문분야/포트폴리오
@RestController
@RequestMapping("/api/contractors")
@RequiredArgsConstructor
public class ContractorProfileController {

	private final ContractorProfileService contractorProfileService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<ContractorProfileResponse>> getMyProfile(Authentication authentication) {
		Long memberId = getMemberId(authentication);
		return ResponseEntity.ok(ApiResponse.success("프로필 조회 완료", contractorProfileService.getOrCreate(memberId)));
	}

	@PutMapping("/me")
	public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody ContractorProfileUpdateRequest request,
			Authentication authentication) {
		Long memberId = getMemberId(authentication);
		contractorProfileService.updateProfile(memberId, request);
		return ResponseEntity.ok(ApiResponse.success("프로필이 저장되었습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
