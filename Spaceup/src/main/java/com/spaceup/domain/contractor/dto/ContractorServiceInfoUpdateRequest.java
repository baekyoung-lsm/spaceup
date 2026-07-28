package com.spaceup.domain.contractor.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ⭐ [시공사 추천 점수] 견적 범위/가능 일정 - 마이페이지에서 시공사가 직접 입력
@Getter
@Setter
@NoArgsConstructor
public class ContractorServiceInfoUpdateRequest {

	private Long estimateMin;
	private Long estimateMax;
	private LocalDate availableFromDate;
}
