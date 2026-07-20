package com.spaceup.domain.settlement.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.settlement.dto.SettlementCreateRequest;
import com.spaceup.domain.settlement.dto.SettlementResponse;
import com.spaceup.domain.settlement.entity.Settlement;
import com.spaceup.domain.settlement.entity.SettlementStatus;
import com.spaceup.domain.settlement.repository.SettlementRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.SettlementNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

	private final SettlementRepository settlementRepository;
	private final MemberRepository memberRepository;

	// ⭐ 관리자 "시스템설정"의 기본 수수료율(10%)을 임시로 상수화했습니다. 실제로는 admin/system-settings 도메인이
	// 생기면 DB 값을 읽어오도록 이 상수 하나만 교체하면 됩니다.
	private static final double DEFAULT_COMMISSION_RATE = 0.10;

	// ⭐ PDF "정산/수수료 관리(관리자)" - 거래 1건에 대한 정산 레코드 생성. 거래금액에서 수수료를 뗀 정산액을 계산합니다.
	@Transactional
	public Long createSettlement(SettlementCreateRequest dto) {
		Member partner = memberRepository.findById(dto.getPartnerId())
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + dto.getPartnerId()));

		long commission = Math.round(dto.getTransactionAmount() * DEFAULT_COMMISSION_RATE);
		long payout = dto.getTransactionAmount() - commission;

		Settlement settlement = Settlement.builder().transactionCode(generateTransactionCode()).partner(partner)
				.transactionAmount(dto.getTransactionAmount()).commissionAmount(commission).payoutAmount(payout)
				.status(SettlementStatus.PENDING).build();

		settlementRepository.save(settlement);
		return settlement.getId();
	}

	// ⭐ PDF "정산 관리(자재업체/시공사)" - 정산 완료 처리
	@Transactional
	public void complete(Long settlementId) {
		findSettlementOrThrow(settlementId).complete();
	}

	public SettlementResponse getSettlement(Long settlementId) {
		return new SettlementResponse(findSettlementOrThrow(settlementId));
	}

	// ⭐ 정산 대상(시공사/자재업체) 로그인 기준 - 본인 정산 내역 조회
	public List<SettlementResponse> getSettlementsByPartner(Long partnerId) {
		return settlementRepository.findByPartnerId(partnerId).stream().map(SettlementResponse::new)
				.collect(Collectors.toList());
	}

	private Settlement findSettlementOrThrow(Long settlementId) {
		return settlementRepository.findById(settlementId)
				.orElseThrow(() -> new SettlementNotFoundException("존재하지 않는 정산 내역입니다: " + settlementId));
	}

	private String generateTransactionCode() {
		String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		long todayCount = settlementRepository.count() + 1;
		return String.format("TR-%s-%03d", datePart, todayCount);
	}
}
