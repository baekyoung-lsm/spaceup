package com.spaceup.domain.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.order.dto.OrderCreateRequest;
import com.spaceup.domain.order.dto.OrderResponse;
import com.spaceup.domain.order.entity.MaterialOrder;
import com.spaceup.domain.order.entity.OrderStatus;
import com.spaceup.domain.order.repository.MaterialOrderRepository;
import com.spaceup.domain.product.entity.Product;
import com.spaceup.domain.product.repository.ProductRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.OrderNotFoundException;
import com.spaceup.global.error.ProductNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	private final MaterialOrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;

	// ⭐ PDF "주문/발주 관리" - 시공사가 자재를 주문하는 시점. 재고를 즉시 차감하고(선점), 결제는 별도 플래그로 관리합니다.
	@Transactional
	public Long createOrder(Long buyerId, OrderCreateRequest dto) {
		Member buyer = memberRepository.findById(buyerId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + buyerId));
		Product product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ProductNotFoundException("존재하지 않는 상품입니다: " + dto.getProductId()));

		// 재고 차감 (음수로 넘겨 출고 처리 - Product.updateStock이 SOLD_OUT 전환까지 같이 처리)
		product.updateStock(-dto.getQuantity());

		long orderAmount = product.getSalePrice() * dto.getQuantity();

		MaterialOrder order = MaterialOrder.builder().orderCode(generateOrderCode()).product(product).buyer(buyer)
				.quantity(dto.getQuantity()).orderAmount(orderAmount).paymentCompleted(false)
				.status(OrderStatus.NEW).build();

		orderRepository.save(order);
		return order.getId();
	}

	// ⭐ PDF "주문/발주 관리" 파이프라인 단계 전환 (자재업체 액션)
	@Transactional
	public void markReady(Long orderId) {
		findOrderOrThrow(orderId).markReady();
	}

	@Transactional
	public void markShipped(Long orderId) {
		findOrderOrThrow(orderId).markShipped();
	}

	@Transactional
	public void complete(Long orderId) {
		findOrderOrThrow(orderId).complete();
	}

	@Transactional
	public void completePayment(Long orderId) {
		findOrderOrThrow(orderId).completePayment();
	}

	public OrderResponse getOrder(Long orderId) {
		return new OrderResponse(findOrderOrThrow(orderId));
	}

	// ⭐ PDF "주문/발주 관리" 목록 (시공사 로그인 기준 - 본인이 주문한 내역)
	public List<OrderResponse> getOrdersByBuyer(Long buyerId) {
		return orderRepository.findByBuyerId(buyerId).stream().map(OrderResponse::new).collect(Collectors.toList());
	}

	// ⭐ PDF "주문/발주 관리" 목록 (자재업체 로그인 기준 - 상태별 파이프라인 조회)
	public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
		return orderRepository.findByStatus(status).stream().map(OrderResponse::new).collect(Collectors.toList());
	}

	private MaterialOrder findOrderOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("존재하지 않는 주문입니다: " + orderId));
	}

	private String generateOrderCode() {
		String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		long todayCount = orderRepository.count() + 1;
		return String.format("ORD-%s-%03d", datePart, todayCount);
	}
}
