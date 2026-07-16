package com.spaceup.domain.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.schedule.entity.ScheduleEvent;

@Repository
public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Long> {

	List<ScheduleEvent> findByContractorId(Long contractorId);
}
