package com.spaceup.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.domain.schedule.dto.ScheduleCreateRequest;
import com.spaceup.domain.schedule.dto.ScheduleResponse;
import com.spaceup.domain.schedule.entity.ScheduleEvent;
import com.spaceup.domain.schedule.entity.ScheduleStatus;
import com.spaceup.domain.schedule.repository.ScheduleEventRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.RequestNotFoundException;
import com.spaceup.global.error.ScheduleNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

	private final ScheduleEventRepository scheduleEventRepository;
	private final RequestRepository requestRepository;
	private final MemberRepository memberRepository;

	// ⭐ PDF "일정관리" 화면 - 견적이 확정(Quote.accept())된 이후 시공사가 착공 일정을 등록하는 시점
	@Transactional
	public Long createSchedule(Long contractorId, ScheduleCreateRequest dto) {
		Member contractor = memberRepository.findById(contractorId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + contractorId));
		Request request = requestRepository.findById(dto.getRequestId())
				.orElseThrow(() -> new RequestNotFoundException("존재하지 않는 의뢰입니다: " + dto.getRequestId()));

		ScheduleEvent event = ScheduleEvent.builder().contractor(contractor).request(request).title(dto.getTitle())
				.scheduledAt(dto.getScheduledAt()).status(ScheduleStatus.SCHEDULED).build();

		scheduleEventRepository.save(event);
		return event.getId();
	}

	// ⭐ PDF "일정관리" 화면의 월간/목록 뷰 (시공사 로그인 기준 - 본인 일정 전체)
	public List<ScheduleResponse> getSchedulesByContractor(Long contractorId) {
		return scheduleEventRepository.findByContractorId(contractorId).stream().map(ScheduleResponse::new)
				.collect(Collectors.toList());
	}

	@Transactional
	public void reschedule(Long scheduleId, LocalDateTime newTime) {
		findScheduleOrThrow(scheduleId).reschedule(newTime);
	}

	@Transactional
	public void start(Long scheduleId) {
		findScheduleOrThrow(scheduleId).start();
	}

	@Transactional
	public void complete(Long scheduleId) {
		findScheduleOrThrow(scheduleId).complete();
	}

	private ScheduleEvent findScheduleOrThrow(Long scheduleId) {
		return scheduleEventRepository.findById(scheduleId)
				.orElseThrow(() -> new ScheduleNotFoundException("존재하지 않는 일정입니다: " + scheduleId));
	}
}
