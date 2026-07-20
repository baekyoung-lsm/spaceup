package com.spaceup.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.entity.MemberRole;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	// ⭐ 현업 로그인 필수 기능: 사용자가 입력한 아이디(username)로 회원 정보를 찾는 메서드입니다.
	// JPA가 메서드 이름을 분석해서 "SELECT * FROM members WHERE username = ?" 쿼리를 자동으로 만들어
	// 줍니다.
	Optional<Member> findByUsername(String username);

	// ⭐ PDF "회원관리(관리자)" - 역할별 전체 목록 (페이지네이션)
	Page<Member> findByRole(MemberRole role, Pageable pageable);

	// ⭐ PDF "시공사관리/자재업체관리(관리자)" - 승인 대기 큐. 보통 규모가 크지 않아 List로 유지합니다.
	List<Member> findByRoleAndApproved(MemberRole role, boolean approved);

	long countByRole(MemberRole role);
}
