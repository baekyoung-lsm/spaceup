package com.spaceup.domain.notification.entity;

// ⭐ PDF "알림센터" 화면 필터 탭(전체/견적/일정) + 실제 발생 이벤트 기준으로 세분화
public enum NotificationType {
	QUOTE, // 새 견적 요청/도착
	SCHEDULE, // 시공 일정 확정/변경
	REQUEST // 의뢰 상태 변경
}
