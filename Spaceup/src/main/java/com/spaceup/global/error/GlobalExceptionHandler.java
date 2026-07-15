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
}
