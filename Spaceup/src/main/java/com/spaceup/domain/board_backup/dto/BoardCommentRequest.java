package com.spaceup.domain.board_backup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class BoardCommentRequest {
	@NotNull(message = "게시글 번호는 필수입니다.")
	private Long boardId;

	@NotNull(message = "회원 번호는 필수입니다.")
	private Long memberId;

	@NotBlank(message = "댓글 내용은 필수 입력 사항입니다.")
	private String content;
}