package com.spaceup.domain.quote.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.quote.dto.QuoteCreateRequest;
import com.spaceup.domain.quote.dto.QuoteItemRequest;
import com.spaceup.domain.quote.dto.QuoteResponse;
import com.spaceup.domain.quote.entity.Quote;
import com.spaceup.domain.quote.entity.QuoteItem;
import com.spaceup.domain.quote.entity.QuoteStatus;
import com.spaceup.domain.quote.repository.QuoteRepository;
import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.repository.RequestRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.QuoteNotFoundException;
import com.spaceup.global.error.RequestNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

	private final QuoteRepository quoteRepository;
	private final RequestRepository requestRepository;
	private final MemberRepository memberRepository;

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

	// ⭐ PDF "견적 제안 보내기" 버튼
	@Transactional
	public void submit(Long quoteId) {
		findQuoteOrThrow(quoteId).submit();
	}

	@Transactional
	public void accept(Long quoteId) {
		findQuoteOrThrow(quoteId).accept();
	}

	@Transactional
	public void reject(Long quoteId) {
		findQuoteOrThrow(quoteId).reject();
	}

	public QuoteResponse getQuote(Long quoteId) {
		return new QuoteResponse(findQuoteOrThrow(quoteId));
	}

	// ⭐ PDF "의뢰 상세" 화면에서 해당 의뢰에 달린 견적(이력) 전체 조회
	public List<QuoteResponse> getQuotesByRequest(Long requestId) {
		return quoteRepository.findByRequestId(requestId).stream().map(QuoteResponse::new)
				.collect(Collectors.toList());
	}

	private long sumByCategory(List<QuoteItemRequest> items) {
		return items.stream().mapToLong(QuoteItemRequest::getAmount).sum();
	}

	private Quote findQuoteOrThrow(Long quoteId) {
		return quoteRepository.findById(quoteId)
				.orElseThrow(() -> new QuoteNotFoundException("존재하지 않는 견적입니다: " + quoteId));
	}
}
