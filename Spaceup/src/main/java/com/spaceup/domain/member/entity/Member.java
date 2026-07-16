package com.spaceup.domain.member.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members") // DB에 'members'라는 이름으로 테이블이 자동 생성됩니다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성을 막는 현업 표준 보안 스타일
@AllArgsConstructor
@Builder
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키(PK) 자동 증가 설정 (MySQL Auto_Increment)
	private Long id;

	@Column(nullable = false, unique = true, length = 50) // 아이디는 필수, 중복 불가
	private String username;

	@Column(nullable = false, length = 100) // 암호화된 비밀번호가 들어가므로 길이를 여유 있게 설정
	private String password;

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false, length = 30)
	private String name;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 가입 일시

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MemberRole role; // ⭐ 로그인 유형(임대인/시공사/자재업체/관리자) - PDF 로그인 화면의 역할 탭에 대응

	// ⭐ 시공사/자재업체는 관리자 승인 전까지 활동이 제한됩니다(PDF 관리자 화면의 "승인/검토중/심사중" 상태).
	// 임대인/관리자는 가입 즉시 true로 취급하고, CONTRACTOR/MATERIAL_VENDOR만 false로 시작합니다.
	@Builder.Default
	@Column(nullable = false)
	private boolean approved = true;

	@Builder.Default
	@Column(nullable = false)
	private boolean withdrawn = false; // 탈퇴 여부 (소프트 삭제 플래그)

	@Column(name = "withdrawn_at")
	private LocalDateTime withdrawnAt; // 탈퇴 처리 일시

	// 데이터가 처음 DB에 저장될 때 가입 시간을 시스템 기준으로 자동으로 주입하는 메서드
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public void updateProfile(String email, String name) {
		this.email = email;
		this.name = name;
	}

	// ⭐ 관리자 승인 처리(시공사/자재업체 전용). 추후 domain/admin 서비스에서 호출합니다.
	public void approve() {
		this.approved = true;
	}

	/**
	 * 소프트 삭제: 계정을 비활성화하고 개인정보를 익명화합니다. username은 유니크 제약 때문에 그대로 두어 재사용을 막고(예약 처리),
	 * 작성한 글/댓글은 이 회원의 name이 바뀌므로 자동으로 "탈퇴한 회원"으로 표시됩니다.
	 */
	public void withdraw() {
		this.name = "탈퇴한 회원";
		this.email = "withdrawn@deleted.local";
		this.withdrawn = true;
		this.withdrawnAt = LocalDateTime.now();
	}
}