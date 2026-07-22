package com.spaceup.domain.portfolio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.portfolio.entity.Portfolio;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	// ⭐ PDF "포트폴리오" 목록 화면 - 시공사 본인의 전체 포트폴리오
	List<Portfolio> findByContractorId(Long contractorId);

	long countByContractorId(Long contractorId);

	long countByContractorIdAndIsPublic(Long contractorId, boolean isPublic);
}
