package com.spaceup.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.product.entity.Product;
import com.spaceup.global.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ⭐ PDF "주문/발주 관리" 화면. buyer는 시공사(자재를 구매하는 쪽), 상품은 자재업체가 등록한 Product.
@Entity
@Table(name = "material_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialOrder extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_code", unique = true, length = 30)
	private String orderCode; // 예: ORD-250714-001

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id", nullable = false)
	private Member buyer; // 주문한 시공사

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "order_amount")
	private Long orderAmount;

	@Column(name = "payment_completed")
	private boolean paymentCompleted;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	public void markShipped() {
		this.status = OrderStatus.SHIPPING;
	}

	public void complete() {
		this.status = OrderStatus.COMPLETED;
	}
}
