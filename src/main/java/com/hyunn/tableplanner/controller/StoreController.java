package com.hyunn.tableplanner.controller;

import com.hyunn.tableplanner.dto.store.*;
import com.hyunn.tableplanner.service.StoreService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 매장 관련 엔드포인트를 관리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 매장을 등록합니다.
     *
     * @param request 매장 등록 요청 정보
     * @return ResponseEntity 등록 성공 메시지
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping("/register")
    public ResponseEntity<String> registerStore(@Valid @RequestBody StoreRegisterRequest request) {
        storeService.registerStore(request);
        return ResponseEntity.ok("Store registered successfully");
    }

    /**
     * 매장을 업데이트합니다.
     *
     * @param request 매장 업데이트 요청 정보
     * @return ResponseEntity 업데이트 성공 메시지
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/update")
    public ResponseEntity<String> updateStore(@Valid @RequestBody StoreUpdateRequest request) {
        storeService.updateStore(request);
        return ResponseEntity.ok("Store updated successfully");
    }

    /**
     * 매장을 삭제합니다.
     *
     * @param request 매장 삭제 요청 정보
     * @return ResponseEntity 삭제 성공 메시지
     */
    @PreAuthorize("hasRole('PARTNER')")
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdrawStore(@Valid @RequestBody StoreDeleteRequest request) {
        storeService.withdrawStore(request);
        return ResponseEntity.ok("Store withdrawn successfully");
    }

    /**
     * 매장 상세 정보를 가져옵니다.
     *
     * @param storeId 매장 ID
     * @return ResponseEntity 매장 상세 정보 (유저 뷰)
     */
    @PermitAll
    @GetMapping("/detail/user/{storeId}")
    public ResponseEntity<StoreDetailUserResponse> getStoreDetailUser(@PathVariable Long storeId) {
        StoreDetailUserResponse response = storeService.getStoreDetailUser(storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 매장 상세 정보를 가져옵니다.
     *
     * @param storeId 매장 ID
     * @return ResponseEntity 매장 상세 정보 (파트너 뷰)
     */
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/detail/partner/{storeId}")
    public ResponseEntity<StoreDetailPartnerResponse> getStoreDetailPartner(@PathVariable Long storeId) {
        StoreDetailPartnerResponse response = storeService.getStoreDetailPartner(storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 매장을 페이지별로 가져옵니다.
     *
     * @param pageable  페이지 요청 정보
     * @param minRating 필터링할 최소 평점
     * @param sortBy    정렬 기준 (rating 또는 reviews)
     * @return ResponseEntity 페이지별 매장 목록
     */
    @PermitAll
    @GetMapping
    public ResponseEntity<Page<StoreSummaryResponse>> getAllStores(Pageable pageable,
                                                                   @RequestParam(defaultValue = "0") Double minRating,
                                                                   @RequestParam(defaultValue = "rating") String sortBy) {
        Page<StoreSummaryResponse> response = storeService.getAllStores(pageable, minRating, sortBy);
        return ResponseEntity.ok(response);
    }
}
