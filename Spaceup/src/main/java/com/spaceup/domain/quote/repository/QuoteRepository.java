package com.spaceup.domain.quote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.quote.entity.Quote;
import com.spaceup.domain.quote.entity.QuoteStatus;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

	List<Quote> findByRequestId(Long requestId);

	List<Quote> findByContractorId(Long contractorId);

	// ⭐ [Figma 반영] 시공사 대시보드 "견적 전송 8건" 카드용
	long countByContractorIdAndStatus(Long contractorId, QuoteStatus status);
}
