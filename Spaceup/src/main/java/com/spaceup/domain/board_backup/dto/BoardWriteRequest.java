package com.spaceup.domain.board_backup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class BoardWriteRequest {
	@NotNull(message = "회원 번호는 필수입니다.")
	private Long memberId;

	@NotBlank(message = "제목은 필수 입력 사항입니다.")
	@Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
	private String title;

	@NotBlank(message = "내용은 필수 입력 사항입니다.")
	private String content;

	@NotBlank(message = "게시판 종류는 필수입니다.")
	private String boardType;
}