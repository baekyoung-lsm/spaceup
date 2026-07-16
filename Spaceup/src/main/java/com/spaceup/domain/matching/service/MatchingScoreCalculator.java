package com.spaceup.domain.matching.service;

import com.spaceup.domain.request.entity.Request;

/**
 * ⭐ PDF 전반에 등장하는 "매칭 점수 92점", "매칭률 88%" 를 계산하는 확장 지점입니다. 규칙 기반으로 갈지, 외부 AI 서버
 * 호출로 갈지 정해지면 구현체를 하나 만들어 @Service로 등록하세요. (예: RuleBasedMatchingScoreCalculator,
 * AiMatchingScoreCalculator) RequestService.assignContractor() 호출 전후로 붙이면 됩니다.
 */
public interface MatchingScoreCalculator {

	// region/propertyType/예산/일정 등을 기준으로 0~100 사이 점수를 산출
	int calculate(Request request, Long contractorId);
}
