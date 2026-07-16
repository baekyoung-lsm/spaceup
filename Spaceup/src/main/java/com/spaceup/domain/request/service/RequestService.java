package com.spaceup.domain.request.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.request.dto.RequestCreateRequest;
import com.spaceup.domain.request.dto.RequestResponse;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.entity.RequestStatus;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.global.error.InvalidStatusTransitionException;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.RequestNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

	private final RequestRepository requestRepository;
	private final MemberRepository memberRepository;

	// ⭐ PDF "02 임대 정보 입력" 완료 시 호출. 이 시점엔 아직 AI 분석 전이라 spaceAnalysis는 비워둡니다.
	// (AI 분석 결과는 별도의 applySpaceAnalysis()로 채워 넣습니다 - ML 파이프라인 연동 지점)
	@Transactional
	public Long createRequest(Long landlordId, RequestCreateRequest dto) {
		Member landlord = memberRepository.findById(landlordId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + landlordId));

		Request request = Request.builder().requestCode(generateRequestCode()).landlord(landlord)
				.region(dto.getRegion()).propertyType(dto.getPropertyType()).areaM2(dto.getAreaM2())
				.deposit(dto.getDeposit()).monthlyRent(dto.getMonthlyRent()).targetRent(dto.getTargetRent())
				.budget(dto.getBudget()).desiredDate(dto.getDesiredDate()).requestedItems(dto.getRequestedItems())
				.status(RequestStatus.NEW).build();

		requestRepository.save(request);
		return request.getId();
	}

	public RequestResponse getRequest(Long requestId) {
		return new RequestResponse(findRequestOrThrow(requestId));
	}

	// ⭐ PDF "의뢰 목록" 화면 - 시공사 관점
	public List<RequestResponse> getRequestsForContractor(Long contractorId) {
		return requestRepository.findByContractorId(contractorId).stream().map(RequestResponse::new)
				.collect(Collectors.toList());
	}

	// ⭐ PDF "마이페이지 - 견적 요청 내역" 화면 - 임대인 관점
	public List<RequestResponse> getRequestsForLandlord(Long landlordId) {
		return requestRepository.findByLandlordId(landlordId).stream().map(RequestResponse::new)
				.collect(Collectors.toList());
	}

	// ⭐ 임대인이 특정 시공사에게 견적을 요청하는 순간(PDF 08 견적 요청) 시공사가 매칭됩니다.
	@Transactional
	public void assignContractor(Long requestId, Long contractorId) {
		Request request = findRequestOrThrow(requestId);
		Member contractor = memberRepository.findById(contractorId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 시공사입니다: " + contractorId));
		request.assignContractor(contractor);
	}

	// ⭐ PDF "의뢰 상세" 화면의 "의뢰 승인" 버튼
	@Transactional
	public void approve(Long requestId) {
		Request request = findRequestOrThrow(requestId);
		validateTransitionable(request, RequestStatus.REVIEWING);
		request.approve();
	}

	// ⭐ PDF "의뢰 상세" 화면의 "의뢰 거절" 버튼
	@Transactional
	public void reject(Long requestId) {
		Request request = findRequestOrThrow(requestId);
		validateTransitionable(request, RequestStatus.REVIEWING);
		request.reject();
	}

	private void validateTransitionable(Request request, RequestStatus expected) {
		if (request.getStatus() != expected) {
			throw new InvalidStatusTransitionException(
					String.format("현재 상태(%s)에서는 처리할 수 없습니다. 예상 상태: %s", request.getStatus(), expected));
		}
	}

	private Request findRequestOrThrow(Long requestId) {
		return requestRepository.findById(requestId)
				.orElseThrow(() -> new RequestNotFoundException("존재하지 않는 의뢰입니다: " + requestId));
	}

	// ⭐ "REQ-260715-012" 형식: REQ-yyMMdd-일련번호(당일 누적 건수+1을 3자리로)
	private String generateRequestCode() {
		String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		long todayCount = requestRepository.count() + 1;
		return String.format("REQ-%s-%03d", datePart, todayCount);
	}
}
