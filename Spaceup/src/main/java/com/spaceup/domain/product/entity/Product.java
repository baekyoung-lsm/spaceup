package com.spaceup.domain.product.entity;

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
import com.spaceup.global.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ⭐ PDF "자재 상품 관리 / 자재 등록" 화면. vendor는 domain/member의 MATERIAL_VENDOR 역할 회원입니다.
// Repository/Service/Controller는 product 도메인 개발 착수 시 request/quote와 동일한 패턴으로 추가하면 됩니다.
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", nullable = false)
	private Member vendor; // 자재업체 (role = MATERIAL_VENDOR)

	@Column(name = "product_code", unique = true, length = 30)
	private String productCode; // 예: STX-600-DX

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductCategory category;

	@Column(length = 50)
	private String spec; // 규격 (예: 600x600x10T)

	@Column(length = 30)
	private String color;

	@Column(name = "supply_price")
	private Long supplyPrice; // 공급가

	@Column(name = "sale_price")
	private Long salePrice; // 판매가

	@Column(name = "min_order_qty")
	private Integer minOrderQty; // 최소 주문 수량

	@Column(name = "stock_qty")
	private Integer stockQty; // 현재 재고

	@Column(length = 50)
	private String manufacturer;

	@Column(length = 30)
	private String brand;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status;

	public void updateStock(int delta) {
		this.stockQty += delta;
		if (this.stockQty <= 0) {
			this.status = ProductStatus.SOLD_OUT;
		}
	}
}
