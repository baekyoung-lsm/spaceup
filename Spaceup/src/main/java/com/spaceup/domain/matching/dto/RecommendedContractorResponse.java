package com.spaceup.domain.matching.dto;

import java.time.LocalDate;

// ⭐ [시공사 추천 점수] "화면에 필요한 데이터" 그대로 매핑
public record RecommendedContractorResponse(Long contractorId, String companyName, Double rating,
		Integer reviewCount, Long estimateMin, Long estimateMax, LocalDate availableDate, int matchScore) {
}
