package com.spaceup.domain.contractor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.contractor.entity.ContractorProfile;

@Repository
public interface ContractorProfileRepository extends JpaRepository<ContractorProfile, Long> {

	Optional<ContractorProfile> findByMemberId(Long memberId);
}
