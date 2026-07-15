package com.spaceup.domain.board_backup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.board_backup.dto.BoardCommentRequest;
import com.spaceup.domain.board_backup.dto.BoardCommentUpdateRequest;
import com.spaceup.domain.board_backup.dto.CommentResponse;
import com.spaceup.domain.board_backup.service.CommentService;
import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/board/comment")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/write")
	public ResponseEntity<ApiResponse<Void>> writeComment(@Valid @RequestBody BoardCommentRequest request) {
		commentService.writeComment(request.getBoardId(), request.getMemberId(), request.getContent());
		return ResponseEntity.ok(ApiResponse.success("댓글 등록 성공!", null));
	}

	@GetMapping("/list/{boardId}")
	public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentList(@PathVariable("boardId") Long boardId) {
		return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 완료", commentService.getCommentsByBoard(boardId)));
	}

	@PutMapping("/{commentId}")
	public ResponseEntity<ApiResponse<Void>> updateComment(@PathVariable Long commentId,
			@Valid @RequestBody BoardCommentUpdateRequest request, Authentication authentication) {
		Long requesterId = getMemberIdFromAuthentication(authentication);
		commentService.updateComment(commentId, requesterId, request.getContent());
		return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", null));
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId,
			Authentication authentication) {
		Long requesterId = getMemberIdFromAuthentication(authentication);
		commentService.deleteComment(commentId, requesterId);
		return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
	}

	private Long getMemberIdFromAuthentication(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}