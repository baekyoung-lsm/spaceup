package com.spaceup.domain.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.notification.dto.NotificationResponse;
import com.spaceup.domain.notification.service.NotificationService;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

// ⭐ 알림 생성은 외부에 노출하지 않습니다(다른 도메인 서비스가 내부적으로 NotificationService.notify()를 호출).
// 이 컨트롤러는 PDF "알림센터" 화면에서 조회/읽음 처리만 담당합니다.
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Authentication authentication) {
		Long receiverId = getMemberId(authentication);
		return ResponseEntity.ok(ApiResponse.success("알림 목록 조회 완료", notificationService.getMyNotifications(receiverId)));
	}

	@PostMapping("/{notificationId}/read")
	public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
		notificationService.markAsRead(notificationId);
		return ResponseEntity.ok(ApiResponse.success("알림을 읽음 처리했습니다.", null));
	}

	// ⭐ PDF "알림센터" 화면의 "모두 읽음" 버튼
	@PostMapping("/read-all")
	public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
		Long receiverId = getMemberId(authentication);
		notificationService.markAllAsRead(receiverId);
		return ResponseEntity.ok(ApiResponse.success("모든 알림을 읽음 처리했습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
