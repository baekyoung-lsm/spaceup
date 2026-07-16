package com.spaceup.domain.request.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.request.entity.Request;
import com.spaceup.domain.request.entity.RequestStatus;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

	Optional<Request> findByRequestCode(String requestCode);

	// ⭐ PDF "의뢰 목록" 화면: 시공사가 본인에게 온 의뢰를 상태별로 조회
	List<Request> findByContractorIdAndStatus(Long contractorId, RequestStatus status);

	List<Request> findByContractorId(Long contractorId);

	// ⭐ PDF "마이페이지 - 견적 요청 내역" 화면: 임대인이 본인이 보낸 의뢰 목록 조회
	List<Request> findByLandlordId(Long landlordId);
}
