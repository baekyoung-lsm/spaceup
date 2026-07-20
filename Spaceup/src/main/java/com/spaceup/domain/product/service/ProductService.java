package com.spaceup.domain.product.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spaceup.domain.member.entity.Member;
import com.spaceup.domain.member.repository.MemberRepository;
import com.spaceup.domain.product.dto.ProductCreateRequest;
import com.spaceup.domain.product.dto.ProductResponse;
import com.spaceup.domain.product.entity.Product;
import com.spaceup.domain.product.entity.ProductStatus;
import com.spaceup.domain.product.repository.ProductRepository;
import com.spaceup.global.error.MemberNotFoundException;
import com.spaceup.global.error.ProductNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;

	// ⭐ PDF "자재 등록" 화면의 "변경사항 저장" 버튼 (신규 등록)
	@Transactional
	public Long register(Long vendorId, ProductCreateRequest dto) {
		Member vendor = memberRepository.findById(vendorId)
				.orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 번호입니다: " + vendorId));

		String productCode = (dto.getProductCode() != null && !dto.getProductCode().isBlank()) ? dto.getProductCode()
				: generateProductCode();

		Product product = Product.builder().vendor(vendor).productCode(productCode).name(dto.getName())
				.category(dto.getCategory()).spec(dto.getSpec()).color(dto.getColor())
				.supplyPrice(dto.getSupplyPrice()).salePrice(dto.getSalePrice()).minOrderQty(dto.getMinOrderQty())
				.stockQty(dto.getStockQty()).manufacturer(dto.getManufacturer()).brand(dto.getBrand())
				.status(dto.getStockQty() != null && dto.getStockQty() > 0 ? ProductStatus.ON_SALE
						: ProductStatus.SOLD_OUT)
				.build();

		productRepository.save(product);
		return product.getId();
	}

	// ⭐ PDF "자재 등록/수정" 화면에서 기존 상품 수정
	@Transactional
	public void update(Long productId, ProductCreateRequest dto) {
		Product product = findProductOrThrow(productId);
		product.updateInfo(dto.getName(), dto.getSpec(), dto.getColor(), dto.getSupplyPrice(), dto.getSalePrice(),
				dto.getMinOrderQty(), dto.getManufacturer(), dto.getBrand());
	}

	// ⭐ PDF "재고 관리" 화면의 "재고 조정" / "입고 등록" 버튼 (delta가 양수면 입고, 음수면 출고/조정)
	@Transactional
	public void adjustStock(Long productId, int delta) {
		findProductOrThrow(productId).updateStock(delta);
	}

	// ⭐ PDF "자재 상품 관리" 화면의 상태 필터(판매중/품절/판매중지) 변경
	@Transactional
	public void changeStatus(Long productId, ProductStatus status) {
		findProductOrThrow(productId).changeStatus(status);
	}

	public ProductResponse getProduct(Long productId) {
		return new ProductResponse(findProductOrThrow(productId));
	}

	// ⭐ PDF "자재 상품 관리" 목록 (자재업체 로그인 기준 - 본인이 등록한 상품만)
	public List<ProductResponse> getProductsByVendor(Long vendorId) {
		return productRepository.findByVendorId(vendorId).stream().map(ProductResponse::new)
				.collect(Collectors.toList());
	}

	private Product findProductOrThrow(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException("존재하지 않는 상품입니다: " + productId));
	}

	// ⭐ "STX-600-DX" 처럼 브랜드별 코드 체계가 있는 경우가 많아, 미입력 시 임시로 날짜 기반 코드를 자동 채번합니다.
	// (실제 운영에서는 브랜드/카테고리별 접두어 정책이 정해지면 이 메서드만 교체하면 됩니다)
	private String generateProductCode() {
		String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
		long todayCount = productRepository.count() + 1;
		return String.format("PRD-%s-%04d", datePart, todayCount);
	}
}
