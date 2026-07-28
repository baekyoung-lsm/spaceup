package com.spaceup.domain.contractor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.contractor.entity.ContractorProfile;

@Repository
public interface ContractorProfileRepository extends JpaRepository<ContractorProfile, Long> {

	Optional<ContractorProfile> findByMemberId(Long memberId);

	// ⭐ [시공사 추천] 신규 상담 가능 상태인 업체만 추천 후보로 조회
	List<ContractorProfile> findByAvailableForConsultTrue();
}
