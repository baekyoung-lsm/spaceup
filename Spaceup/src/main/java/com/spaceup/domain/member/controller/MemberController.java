package com.spaceup.domain.member.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.dto.LoginRequest;
import com.spaceup.domain.member.dto.MemberJoinRequest;
import com.spaceup.domain.member.dto.MemberResponse;
import com.spaceup.domain.member.dto.MemberUpdateRequest;
import com.spaceup.domain.member.dto.PhoneUpdateRequest;
import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.member.service.MemberService;
import com.spaceup.global.error.UnauthorizedAccessException;
import com.spaceup.global.security.JwtTokenProvider;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/join")
	public ResponseEntity<ApiResponse<Void>> join(@Valid @RequestBody MemberJoinRequest request) {
		Member member = Member.builder().username(request.getUsername()).password(request.getPassword())
				.email(request.getEmail()).name(request.getName()).phoneNumber(request.getPhoneNumber())
				.role(request.getRole()).build();
		memberService.join(member);
		return ResponseEntity.ok(ApiResponse.success("회원가입이 완벽하게 완료되었습니다.", null));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {
		if (memberService.login(loginRequest.getUsername(), loginRequest.getPassword())) {
			String token = jwtTokenProvider.createToken(loginRequest.getUsername());
			return ResponseEntity.ok(ApiResponse.success("로그인 성공!", token));
		}
		return ResponseEntity.status(401).body(ApiResponse.fail("로그인 실패: 아이디 또는 비밀번호가 틀렸습니다."));
	}

	@GetMapping("/{memberId}")
	public ResponseEntity<ApiResponse<MemberResponse>> getProfile(@PathVariable Long memberId) {
		return ResponseEntity.ok(ApiResponse.success("회원정보 조회 완료", memberService.getProfile(memberId)));
	}

	@PutMapping("/{memberId}")
	public ResponseEntity<ApiResponse<Void>> updateProfile(@PathVariable Long memberId,
			@Valid @RequestBody MemberUpdateRequest request, Authentication authentication) {
		Long requesterId = getMemberIdFromAuthentication(authentication);
		if (!requesterId.equals(memberId)) {
			throw new UnauthorizedAccessException("본인 정보만 수정할 수 있습니다.");
		}
		memberService.updateProfile(memberId, request.getEmail(), request.getName());
		return ResponseEntity.ok(ApiResponse.success("회원정보가 수정되었습니다.", null));
	}

	// ⭐ [Figma 반영] 마이페이지 - 계정설정의 "휴대폰 번호 변경". 실제 SMS 재인증은 아직 없어 변경 즉시
	// phoneVerified=false로만 표시됩니다 (OTP 연동 전까지의 임시 동작).
	@PatchMapping("/me/phone")
	public ResponseEntity<ApiResponse<Void>> updatePhone(@Valid @RequestBody PhoneUpdateRequest request,
			Authentication authentication) {
		Long memberId = getMemberIdFromAuthentication(authentication);
		memberService.updatePhoneNumber(memberId, request.getPhoneNumber());
		return ResponseEntity.ok(ApiResponse.success("휴대폰 번호가 변경되었습니다. 재인증이 필요합니다.", null));
	}

	// ⭐ [Figma 반영] "보완 요청" 화면의 "보완 자료 재제출" 버튼 - 본인만, NEEDS_REVISION 상태에서만 가능
	@PostMapping("/me/resubmit")
	public ResponseEntity<ApiResponse<Void>> resubmit(Authentication authentication) {
		Long memberId = getMemberIdFromAuthentication(authentication);
		memberService.resubmit(memberId);
		return ResponseEntity.ok(ApiResponse.success("재제출이 완료되었습니다. 심사 중입니다.", null));
	}

	@DeleteMapping("/{memberId}")
	public ResponseEntity<ApiResponse<Void>> withdraw(@PathVariable Long memberId, Authentication authentication) {
		Long requesterId = getMemberIdFromAuthentication(authentication);
		if (!requesterId.equals(memberId)) {
			throw new UnauthorizedAccessException("본인만 탈퇴할 수 있습니다.");
		}
		memberService.withdraw(memberId);
		return ResponseEntity.ok(ApiResponse.success("탈퇴가 완료되었습니다.", null));
	}

	private Long getMemberIdFromAuthentication(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
