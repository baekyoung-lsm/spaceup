package com.spaceup.domain.product.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.spaceup.domain.member.security.MemberPrincipal;
import com.spaceup.domain.product.dto.ProductCreateRequest;
import com.spaceup.domain.product.dto.ProductResponse;
import com.spaceup.domain.product.dto.StockAdjustRequest;
import com.spaceup.domain.product.entity.ProductStatus;
import com.spaceup.domain.product.service.ProductService;
import com.spaceup.global.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// ⭐ PDF "자재 등록" 화면 (자재업체 로그인 기준)
	@PostMapping
	public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody ProductCreateRequest request,
			Authentication authentication) {
		Long vendorId = getMemberId(authentication);
		Long productId = productService.register(vendorId, request);
		return ResponseEntity.ok(ApiResponse.success("상품이 등록되었습니다.", productId));
	}

	// ⭐ PDF "자재 등록/수정" 화면
	@PutMapping("/{productId}")
	public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long productId,
			@Valid @RequestBody ProductCreateRequest request) {
		productService.update(productId, request);
		return ResponseEntity.ok(ApiResponse.success("상품 정보가 수정되었습니다.", null));
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
		return ResponseEntity.ok(ApiResponse.success("상품 조회 완료", productService.getProduct(productId)));
	}

	// ⭐ PDF "자재 상품 관리" 목록 화면 (자재업체 로그인 기준 - 본인 상품만)
	@GetMapping("/vendor/me")
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts(Authentication authentication) {
		Long vendorId = getMemberId(authentication);
		return ResponseEntity.ok(ApiResponse.success("상품 목록 조회 완료", productService.getProductsByVendor(vendorId)));
	}

	// ⭐ PDF "재고 관리" 화면의 "재고 조정" 버튼
	@PostMapping("/{productId}/stock")
	public ResponseEntity<ApiResponse<Void>> adjustStock(@PathVariable Long productId,
			@Valid @RequestBody StockAdjustRequest request) {
		productService.adjustStock(productId, request.getDelta());
		return ResponseEntity.ok(ApiResponse.success("재고가 조정되었습니다.", null));
	}

	// ⭐ PDF "자재 상품 관리" 화면의 판매중지/재개 버튼
	@PatchMapping("/{productId}/status/{status}")
	public ResponseEntity<ApiResponse<Void>> changeStatus(@PathVariable Long productId,
			@PathVariable ProductStatus status) {
		productService.changeStatus(productId, status);
		return ResponseEntity.ok(ApiResponse.success("상품 상태가 변경되었습니다.", null));
	}

	private Long getMemberId(Authentication authentication) {
		MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
		return principal.getId();
	}
}
