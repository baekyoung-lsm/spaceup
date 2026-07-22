package com.spaceup.domain.quote.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.notification.entity.NotificationType;
import com.spaceup.domain.notification.service.NotificationService;
import com.spaceup.domain.quote.dto.QuoteCreateRequest;
import com.spaceup.domain.quote.dto.QuoteItemRequest;
import com.spaceup.domain.quote.dto.QuoteResponse;
import com.spaceup.domain.quote.entity.Quote;
import com.spaceup.domain.quote.entity.QuoteItem;
import com.spaceup.domain.quote.entity.QuoteStatus;
import com.spaceup.domain.quote.repository.QuoteRepository;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.global.error.ForbiddenAccessException;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.QuoteNotFoundException;
import com.spaceup.global.error.RequestNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

	// ⭐ [Figma 반영] 기본 유효기간: 발송일로부터 14일 (화면 예시상 07.15 발송 → 07.31 만료와 유사한 기본값)
	private static final int DEFAULT_VALIDITY_DAYS = 14;

	private final QuoteRepository quoteRepository;
	private final RequestRepository requestRepository;
	private final MemberRepository memberRepository;
	private final NotificationService notificationService;

	// ⭐ PDF "임시 저장" 버튼 → DRAFT 상태로 생성. 항목 금액 합계 + 부가세 - 할인 = 최종 견적으로 자동 계산합니다.
	@Transactional
	public Long createDraft(Long contractorId, QuoteCreateRequest dto) {
		Request request = requestRepository.findById(dto.getRequestId())
				.orElseThrow(() -> new RequestNotFoundException("존재하지 않는 의뢰입니다: " + dto.getRequestId()));
		Member contractor = memberRepository.findById(contractorId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 시공사입니다: " + contractorId));

		long materialCost = sumByCategory(dto.getItems());

		Quote quote = Quote.builder().request(request).contractor(contractor).title(dto.getTitle())
				.startDate(dto.getStartDate()).durationDays(dto.getDurationDays())
				.materialCost(dto.getMaterialCost() != null ? dto.getMaterialCost() : materialCost)
				.laborCost(dto.getLaborCost()).vat(dto.getVat()).discount(dto.getDiscount())
				.detailContent(dto.getDetailContent()).status(QuoteStatus.DRAFT).build();

		dto.getItems().forEach(itemDto -> quote
				.addItem(QuoteItem.builder().category(itemDto.getCategory()).description(itemDto.getDescription())
						.amount(itemDto.getAmount()).build()));

		quote.recalculateTotal();
		quoteRepository.save(quote);
		return quote.getId();
	}

	// ⭐ PDF "견적 제안 보내기" 버튼 - 작성한 시공사 본인만 발송 가능. 임대인에게 알림
	// ⭐ [Figma 반영] 발송 시점에 유효기간(validUntil)이 없으면 기본 14일로 채웁니다.
	@Transactional
	public void submit(Long quoteId, Long contractorId) {
		Quote quote = findQuoteOrThrow(quoteId);
		validateContractorOwnership(quote, contractorId);
		quote.submit();
		if (quote.getValidUntil() == null) {
			quote.extendValidUntil(LocalDate.now().plusDays(DEFAULT_VALIDITY_DAYS));
		}

		notificationService.notify(quote.getRequest().getLandlord().getId(), NotificationType.QUOTE, "새 견적이 도착했습니다",
				String.format("%s님이 %,d원 견적을 보냈습니다.", quote.getContractor().getName(), quote.getTotalAmount()));
	}

	// ⭐ 임대인이 최종 선택 - 해당 의뢰의 임대인 본인만 가능. 시공사에게 알림
	@Transactional
	public void accept(Long quoteId, Long landlordId) {
		Quote quote = findQuoteOrThrow(quoteId);
		validateLandlordOwnership(quote, landlordId);
		quote.accept();

		notificationService.notify(quote.getContractor().getId(), NotificationType.QUOTE, "견적이 선택되었습니다",
				String.format("%s 견적이 최종 선택되었습니다. 일정을 등록해 주세요.", quote.getTitle()));
	}

	@Transactional
	public void reject(Long quoteId, Long landlordId) {
		Quote quote = findQuoteOrThrow(quoteId);
		validateLandlordOwnership(quote, landlordId);
		quote.reject();

		notificationService.notify(quote.getContractor().getId(), NotificationType.QUOTE, "견적이 거절되었습니다",
				String.format("%s 견적이 거절되었습니다.", quote.getTitle()));
	}

	// ⭐ [Figma 반영] "유효기간 연장" 화면 - 작성한 시공사 본인만 가능
	@Transactional
	public void extendValidity(Long quoteId, Long contractorId, LocalDate newValidUntil) {
		Quote quote = findQuoteOrThrow(quoteId);
		validateContractorOwnership(quote, contractorId);
		quote.extendValidUntil(newValidUntil);

		notificationService.notify(quote.getRequest().getLandlord().getId(), NotificationType.QUOTE, "견적 유효기간이 연장되었습니다",
				String.format("%s 견적의 유효기간이 %s까지 연장되었습니다.", quote.getTitle(), newValidUntil));
	}

	// ⭐ [Figma 반영] "보낸 견적 상세 - 수정 요청" 화면 - 해당 의뢰의 임대인 본인만 가능. 시공사에게 알림
	@Transactional
	public void requestRevision(Long quoteId, Long landlordId, String note) {
		Quote quote = findQuoteOrThrow(quoteId);
		validateLandlordOwnership(quote, landlordId);
		quote.requestRevision(note);

		notificationService.notify(quote.getContractor().getId(), NotificationType.QUOTE, "견적 수정 요청이 도착했습니다",
				String.format("%s 견적에 대한 수정 요청: %s", quote.getTitle(), note));
	}

	public QuoteResponse getQuote(Long quoteId) {
		return new QuoteResponse(findQuoteOrThrow(quoteId));
	}

	// ⭐ PDF "의뢰 상세" 화면에서 해당 의뢰에 달린 견적(이력) 전체 조회
	public List<QuoteResponse> getQuotesByRequest(Long requestId) {
		return quoteRepository.findByRequestId(requestId).stream().map(QuoteResponse::new)
				.collect(Collectors.toList());
	}

	private void validateContractorOwnership(Quote quote, Long contractorId) {
		if (!quote.getContractor().getId().equals(contractorId)) {
			throw new ForbiddenAccessException("본인이 작성한 견적만 처리할 수 있습니다.");
		}
	}

	private void validateLandlordOwnership(Quote quote, Long landlordId) {
		if (!quote.getRequest().getLandlord().getId().equals(landlordId)) {
			throw new ForbiddenAccessException("본인이 등록한 의뢰의 견적만 처리할 수 있습니다.");
		}
	}

	private long sumByCategory(List<QuoteItemRequest> items) {
		return items.stream().mapToLong(QuoteItemRequest::getAmount).sum();
	}

	private Quote findQuoteOrThrow(Long quoteId) {
		return quoteRepository.findById(quoteId)
				.orElseThrow(() -> new QuoteNotFoundException("존재하지 않는 견적입니다: " + quoteId));
	}
}
