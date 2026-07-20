package com.spaceup.domain.settlement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.settlement.entity.Settlement;
import com.spaceup.domain.settlement.entity.SettlementStatus;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

	Page<Settlement> findByPartnerId(Long partnerId, Pageable pageable);

	long countByStatus(SettlementStatus status);
}
