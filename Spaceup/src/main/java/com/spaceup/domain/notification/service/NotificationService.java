package com.spaceup.domain.notification.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.notification.dto.NotificationResponse;
import com.spaceup.domain.notification.entity.Notification;
import com.spaceup.domain.notification.entity.NotificationType;
import com.spaceup.domain.notification.repository.NotificationRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.NotificationNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;

	// ⭐ 이 메서드가 핵심 확장 지점입니다. RequestService.assignContractor(), QuoteService.submit(),
	// ScheduleEvent 생성 시점 등에서 이 메서드를 호출해 알림을 자동 발생시키면 됩니다.
	// 예: notificationService.notify(contractor.getId(), NotificationType.REQUEST, "새 의뢰가 도착했습니다", ...)
	@Transactional
	public Long notify(Long receiverId, NotificationType type, String title, String content) {
		Member receiver = memberRepository.findById(receiverId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + receiverId));

		Notification notification = Notification.builder().receiver(receiver).type(type).title(title)
				.content(content).build();

		notificationRepository.save(notification);
		return notification.getId();
	}

	// ⭐ PDF "알림센터" 화면 목록 (로그인한 본인 알림, 최신순)
	public List<NotificationResponse> getMyNotifications(Long receiverId) {
		return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
				.map(NotificationResponse::new).collect(Collectors.toList());
	}

	@Transactional
	public void markAsRead(Long notificationId) {
		findNotificationOrThrow(notificationId).markAsRead();
	}

	// ⭐ PDF "알림센터" 화면의 "모두 읽음" 버튼
	@Transactional
	public void markAllAsRead(Long receiverId) {
		notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId).forEach(Notification::markAsRead);
	}

	private Notification findNotificationOrThrow(Long notificationId) {
		return notificationRepository.findById(notificationId)
				.orElseThrow(() -> new NotificationNotFoundException("존재하지 않는 알림입니다: " + notificationId));
	}
}
