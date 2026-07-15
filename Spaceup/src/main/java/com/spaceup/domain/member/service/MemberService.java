package com.spaceup.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.dto.MemberResponse;
import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.global.error.DuplicateMemberException;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.WithdrawnMemberException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Long join(Member member) {
		validateDuplicateMember(member.getUsername());
		String encodedPassword = passwordEncoder.encode(member.getPassword());

		Member encryptedMember = Member.builder().username(member.getUsername()).password(encodedPassword)
				.email(member.getEmail()).name(member.getName()).build();

		memberRepository.save(encryptedMember);
		return encryptedMember.getId();
	}

	public boolean login(String username, String rawPassword) {
		Member member = memberRepository.findByUsername(username).orElse(null);
		if (member == null) {
			return false;
		}
		if (member.isWithdrawn()) {
			// ⭐ 소프트 삭제된 회원: 비밀번호가 맞더라도 로그인 자체를 차단하고 명확한 사유를 안내
			throw new WithdrawnMemberException("이미 탈퇴한 계정입니다: " + username);
		}
		return passwordEncoder.matches(rawPassword, member.getPassword());
	}

	private void validateDuplicateMember(String username) {
		memberRepository.findByUsername(username).ifPresent(m -> {
			throw new DuplicateMemberException("이미 존재하는 아이디입니다: " + username);
		});
	}

	public MemberResponse getProfile(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + memberId));
		return new MemberResponse(member);
	}

	@Transactional
	public void updateProfile(Long memberId, String email, String name) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + memberId));
		member.updateProfile(email, name);
	}

	@Transactional
	public void withdraw(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + memberId));
		if (member.isWithdrawn()) {
			throw new WithdrawnMemberException("이미 탈퇴 처리된 계정입니다: " + memberId);
		}
		// ⭐ 소프트 삭제: 직접 삭제(delete) 대신 상태만 변경합니다.
		// 작성한 글/댓글이 Member를 FK로 참조하고 있어 하드 삭제 시 제약조건 위반이 발생하기 때문이며,
		// 변경 감지(dirty checking)로 트랜잭션 커밋 시점에 자동 반영되므로 별도 save() 호출이 불필요합니다.
		member.withdraw();
	}
}