package com.spaceup.domain.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	// ⭐ 현업 로그인 필수 기능: 사용자가 입력한 아이디(username)로 회원 정보를 찾는 메서드입니다.
	// JPA가 메서드 이름을 분석해서 "SELECT * FROM members WHERE username = ?" 쿼리를 자동으로 만들어
	// 줍니다.
	Optional<Member> findByUsername(String username);
}
