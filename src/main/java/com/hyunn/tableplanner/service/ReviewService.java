package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.review.ReviewCreateRequest;
import com.hyunn.tableplanner.dto.review.ReviewResponse;
import com.hyunn.tableplanner.dto.review.ReviewUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * ReviewService 인터페이스입니다.
 * 리뷰 관련 비즈니스 로직을 정의합니다.
 */
public interface ReviewService {

    /**
     * 리뷰를 생성합니다.
     *
     * @param request 리뷰 생성 요청 정보
     */
    void createReview(ReviewCreateRequest request);

    /**
     * 리뷰를 업데이트합니다.
     *
     * @param request 리뷰 업데이트 요청 정보
     */
    void updateReview(ReviewUpdateRequest request);

    /**
     * 리뷰를 삭제합니다.
     *
     * @param id 리뷰 ID
     */
    void deleteReview(Long id);

    /**
     * 특정 매장의 모든 리뷰를 조회합니다.
     *
     * @param storeId 매장 ID
     * @param sort 정렬 기준 (옵션)
     * @param minRating 최소 평점 (옵션)
     * @param maxRating 최대 평점 (옵션)
     * @param pageable 페이징 정보
     * @return 매장의 리뷰 목록
     */
    Page<ReviewResponse> getStoreReviews(Long storeId,
                                         Optional<String> sort,
                                         Optional<Integer> minRating,
                                         Optional<Integer> maxRating,
                                         Pageable pageable);

    /**
     * 특정 사용자의 모든 리뷰를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param sort 정렬 기준 (옵션)
     * @param pageable 페이징 정보
     * @return 사용자의 리뷰 목록
     */
    Page<ReviewResponse> getUserReviews(Long userId, Optional<String> sort, Pageable pageable);
}
