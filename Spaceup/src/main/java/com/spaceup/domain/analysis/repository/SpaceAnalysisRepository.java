package com.spaceup.domain.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.analysis.entity.SpaceAnalysis;

@Repository
public interface SpaceAnalysisRepository extends JpaRepository<SpaceAnalysis, Long> {

	Optional<SpaceAnalysis> findByRequestId(Long requestId);
}
