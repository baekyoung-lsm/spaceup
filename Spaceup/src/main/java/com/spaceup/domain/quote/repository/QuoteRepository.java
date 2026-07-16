package com.spaceup.domain.quote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.quote.entity.Quote;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

	List<Quote> findByRequestId(Long requestId);

	List<Quote> findByContractorId(Long contractorId);
}
