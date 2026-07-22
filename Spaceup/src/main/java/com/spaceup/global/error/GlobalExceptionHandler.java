package com.spaceup.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // 👈 검증 예외 부품 링크
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.spaceup.global.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// ⭐ 2차 고도화 추가: 형식 검증 에러(@Valid) 발생 시 첫 번째 오타 원인 문구만 낚아채 응답하는 포획선 [우선순위]
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		// 가방 안에 쌓인 오타 원인 명단 중 맨 첫 번째 실패 문구(message)를 추출
		String defaultMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		log.warn("데이터 검증 실패: {}", defaultMessage);
		return ResponseEntity.status(400).body(ApiResponse.fail(defaultMessage));
	}

	@ExceptionHandler(DuplicateMemberException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateMemberException(DuplicateMemberException e) {
		log.warn("회원가입 실패 (아이디 중복): {}", e.getMessage());
		return ResponseEntity.status(400).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(MemberNotFoundException e) {
		log.warn("회원 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(BoardNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleBoardNotFoundException(BoardNotFoundException e) {
		log.warn("게시글 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllException(Exception e) {
		log.error("시스템 치명적 에러 발생: ", e);
		return ResponseEntity.status(500).body(ApiResponse.fail("서버 내부 시스템 오류가 발생했습니다. 관리자에게 문의하세요."));
	}

	@ExceptionHandler(CommentNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleCommentNotFoundException(CommentNotFoundException e) {
		log.warn("댓글 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
		log.warn("권한 없는 접근 시도: {}", e.getMessage());
		return ResponseEntity.status(403).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(FileNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleFileNotFoundException(FileNotFoundException e) {
		log.warn("파일 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(WithdrawnMemberException.class)
	public ResponseEntity<ApiResponse<Void>> handleWithdrawnMemberException(WithdrawnMemberException e) {
		log.warn("탈퇴 회원 로그인 시도: {}", e.getMessage());
		return ResponseEntity.status(403).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(RequestNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleRequestNotFoundException(RequestNotFoundException e) {
		log.warn("의뢰 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(QuoteNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleQuoteNotFoundException(QuoteNotFoundException e) {
		log.warn("견적 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(InvalidStatusTransitionException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidStatusTransitionException(
			InvalidStatusTransitionException e) {
		log.warn("잘못된 상태 변경 시도: {}", e.getMessage());
		return ResponseEntity.status(409).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException e) {
		log.warn("상품 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleOrderNotFoundException(OrderNotFoundException e) {
		log.warn("주문 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(SettlementNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleSettlementNotFoundException(SettlementNotFoundException e) {
		log.warn("정산 내역 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotificationNotFoundException(NotificationNotFoundException e) {
		log.warn("알림 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(ScheduleNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleScheduleNotFoundException(ScheduleNotFoundException e) {
		log.warn("일정 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(InvalidRoleException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidRoleException(InvalidRoleException e) {
		log.warn("허용되지 않은 역할의 접근: {}", e.getMessage());
		return ResponseEntity.status(403).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(AnalysisNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleAnalysisNotFoundException(AnalysisNotFoundException e) {
		log.warn("분석 결과 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(SettingNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleSettingNotFoundException(SettingNotFoundException e) {
		log.warn("시스템 설정 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(ForbiddenAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleForbiddenAccessException(ForbiddenAccessException e) {
		log.warn("본인 소유가 아닌 리소스 접근 시도: {}", e.getMessage());
		return ResponseEntity.status(403).body(ApiResponse.fail(e.getMessage()));
	}

	// ⭐ [최종 검토 반영] 재고 부족(오버셀 시도) 예외 핸들러. 상태 충돌 성격이라 409로 통일.
	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ApiResponse<Void>> handleInsufficientStockException(InsufficientStockException e) {
		log.warn("재고 부족: {}", e.getMessage());
		return ResponseEntity.status(409).body(ApiResponse.fail(e.getMessage()));
	}

	// ⭐ [Figma 반영] 포트폴리오 없음 예외 핸들러
	@ExceptionHandler(PortfolioNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handlePortfolioNotFoundException(PortfolioNotFoundException e) {
		log.warn("포트폴리오 조회 실패: {}", e.getMessage());
		return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
	}
}
