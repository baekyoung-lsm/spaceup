package com.spaceup.domain.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spaceup.domain.order.entity.MaterialOrder;
import com.spaceup.domain.order.entity.OrderStatus;

@Repository
public interface MaterialOrderRepository extends JpaRepository<MaterialOrder, Long> {

	List<MaterialOrder> findByBuyerId(Long buyerId);

	List<MaterialOrder> findByStatus(OrderStatus status);
}
