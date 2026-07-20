package com.spaceup.domain.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.admin.dto.AdminDashboardResponse;
import com.spaceup.domain.admin.dto.SystemSettingResponse;
import com.spaceup.domain.admin.entity.SystemSetting;
import com.spaceup.domain.admin.repository.SystemSettingRepository;
import com.spaceup.domain.member.dto.MemberResponse;
import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.entity.MemberRole;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.domain.settlement.entity.SettlementStatus;
import com.spaceup.domain.settlement.repository.SettlementRepository;
import com.spaceup.global.error.InvalidRoleException;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.SettingNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

	private final MemberRepository memberRepository;
	private final SystemSettingRepository systemSettingRepository;
	private final RequestRepository requestRepository;
	private final SettlementRepository settlementRepository;

	// ⭐ PDF "전체 운영 현황" 대시보드 상단 요약 카드
	public AdminDashboardResponse getDashboard() {
		return new AdminDashboardResponse(memberRepository.countByRole(MemberRole.LANDLORD),
				memberRepository.countByRole(MemberRole.CONTRACTOR),
				memberRepository.countByRole(MemberRole.MATERIAL_VENDOR),
				memberRepository.findByRoleAndApproved(MemberRole.CONTRACTOR, false).size(),
				memberRepository.findByRoleAndApproved(MemberRole.MATERIAL_VENDOR, false).size(),
				requestRepository.count(), settlementRepository.countByStatus(SettlementStatus.PENDING));
	}

	// ⭐ PDF "회원관리" 화면 - role로 필터링해서 전체 목록 조회 (role 미지정 시 전체, 페이지네이션)
	public Page<MemberResponse> getMembers(MemberRole role, Pageable pageable) {
		Page<Member> members = (role != null) ? memberRepository.findByRole(role, pageable)
				: memberRepository.findAll(pageable);
		return members.map(MemberResponse::new);
	}

	// ⭐ PDF "시공사관리 / 자재업체관리" 화면 - 승인 대기 목록
	public List<MemberResponse> getPendingApprovals(MemberRole role) {
		validateApprovableRole(role);
		return memberRepository.findByRoleAndApproved(role, false).stream().map(MemberResponse::new)
				.collect(Collectors.toList());
	}

	// ⭐ PDF "시공사관리 / 자재업체관리" 화면의 "승인" 버튼
	@Transactional
	public void approveMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + memberId));
		validateApprovableRole(member.getRole());
		member.approve();
	}

	private void validateApprovableRole(MemberRole role) {
		if (role != MemberRole.CONTRACTOR && role != MemberRole.MATERIAL_VENDOR) {
			throw new InvalidRoleException("승인 대상은 시공사 또는 자재업체만 가능합니다.");
		}
	}

	// ⭐ PDF "시스템설정" 화면 - 수수료율 등 조회
	public SystemSettingResponse getSetting(String key) {
		return new SystemSettingResponse(findSettingOrThrow(key));
	}

	// ⭐ PDF "시스템설정" 화면 - 값 변경 (없으면 새로 생성 = upsert)
	@Transactional
	public void updateSetting(String key, String value, String description) {
		systemSettingRepository.findBySettingKey(key).ifPresentOrElse(setting -> setting.updateValue(value),
				() -> systemSettingRepository
						.save(SystemSetting.builder().settingKey(key).settingValue(value).description(description).build()));
	}

	private SystemSetting findSettingOrThrow(String key) {
		return systemSettingRepository.findBySettingKey(key)
				.orElseThrow(() -> new SettingNotFoundException("존재하지 않는 설정입니다: " + key));
	}
}
