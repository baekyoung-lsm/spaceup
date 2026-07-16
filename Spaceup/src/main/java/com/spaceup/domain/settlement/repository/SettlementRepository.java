package com.spaceup.domain.settlement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.settlement.entity.Settlement;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

	List<Settlement> findByPartnerId(Long partnerId);
}
