package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.store.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * StoreService 인터페이스입니다.
 * 매장 관련 비즈니스 로직을 정의합니다.
 */
public interface StoreService {

    /**
     * 매장을 등록합니다.
     *
     * @param request 매장 등록 요청 정보
     */
    void registerStore(StoreRegisterRequest request);

    /**
     * 매장 정보를 수정합니다.
     *
     * @param request 매장 업데이트 요청 정보
     */
    void updateStore(StoreUpdateRequest request);

    /**
     * 매장을 삭제합니다.
     *
     * @param request 매장 삭제 요청 정보
     */
    void withdrawStore(StoreDeleteRequest request);

    /**
     * 매장 상세 정보를 가져옵니다 (파트너 뷰).
     *
     * @param storeId 매장 ID
     * @return StoreDetailPartnerResponse 매장 상세 정보 (파트너 뷰)
     */
    StoreDetailPartnerResponse getStoreDetailPartner(Long storeId);

    /**
     * 매장 상세 정보를 가져옵니다 (유저 뷰).
     *
     * @param storeId 매장 ID
     * @return StoreDetailUserResponse 매장 상세 정보 (유저 뷰)
     */
    StoreDetailUserResponse getStoreDetailUser(Long storeId);

    /**
     * 모든 매장을 페이지별로 가져옵니다.
     *
     * @param pageable  페이지 요청 정보
     * @param minRating 필터링할 최소 평점
     * @param sortBy    정렬 기준 (rating 또는 reviews)
     * @return Page<StoreSummaryResponse> 페이지별 매장 목록
     */
    Page<StoreSummaryResponse> getAllStores(Pageable pageable, Double minRating, String sortBy);
}
