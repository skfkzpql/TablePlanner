package com.hyunn.tableplanner.controller;

import com.hyunn.tableplanner.dto.review.ReviewCreateRequest;
import com.hyunn.tableplanner.dto.review.ReviewResponse;
import com.hyunn.tableplanner.dto.review.ReviewUpdateRequest;
import com.hyunn.tableplanner.service.ReviewService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 리뷰 관련 엔드포인트를 관리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 리뷰를 생성합니다.
     *
     * @param reviewCreateRequest 리뷰 생성 요청 정보
     * @return 생성 성공 메시지
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createReview(@RequestBody ReviewCreateRequest reviewCreateRequest) {
        reviewService.createReview(reviewCreateRequest);
        return ResponseEntity.ok("Review created successfully");
    }

    /**
     * 리뷰를 업데이트합니다.
     *
     * @param id 리뷰 ID
     * @param reviewUpdateRequest 리뷰 업데이트 요청 정보
     * @return 업데이트 성공 메시지
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateReview(@PathVariable Long id,
                                               @RequestBody ReviewUpdateRequest reviewUpdateRequest) {
        reviewService.updateReview(reviewUpdateRequest);
        return ResponseEntity.ok("Review updated successfully");
    }

    /**
     * 리뷰를 삭제합니다.
     *
     * @param id 리뷰 ID
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }

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
    @GetMapping("/store/{storeId}")
    @PermitAll
    public ResponseEntity<Page<ReviewResponse>> getStoreReviews(@PathVariable Long storeId,
                                                                @RequestParam Optional<String> sort,
                                                                @RequestParam Optional<Integer> minRating,
                                                                @RequestParam Optional<Integer> maxRating,
                                                                Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getStoreReviews(storeId, sort, minRating, maxRating, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 사용자의 모든 리뷰를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param sort 정렬 기준 (옵션)
     * @param pageable 페이징 정보
     * @return 사용자의 리뷰 목록
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewResponse>> getUserReviews(@PathVariable Long userId,
                                                               @RequestParam Optional<String> sort,
                                                               Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getUserReviews(userId, sort, pageable);
        return ResponseEntity.ok(reviews);
    }
}
