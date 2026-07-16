package com.spaceup.domain.member.dto;

import java.time.LocalDateTime;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.entity.MemberRole;

import lombok.Getter;

@Getter
public class MemberResponse {
	private final Long id;
	private final String username;
	private final String email;
	private final String name;
	private final MemberRole role;
	private final boolean approved;
	private final LocalDateTime createdAt;

	public MemberResponse(Member member) {
		this.id = member.getId();
		this.username = member.getUsername();
		this.email = member.getEmail();
		this.name = member.getName();
		this.role = member.getRole();
		this.approved = member.isApproved();
		this.createdAt = member.getCreatedAt();
	}
}