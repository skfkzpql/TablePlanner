package com.hyunn.tableplanner.service.impl;

import com.hyunn.tableplanner.dto.review.ReviewCreateRequest;
import com.hyunn.tableplanner.dto.review.ReviewResponse;
import com.hyunn.tableplanner.dto.review.ReviewUpdateRequest;
import com.hyunn.tableplanner.exception.ReservationException;
import com.hyunn.tableplanner.exception.ReviewException;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.Reservation;
import com.hyunn.tableplanner.model.Review;
import com.hyunn.tableplanner.model.Store;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.model.types.ReservationStatus;
import com.hyunn.tableplanner.repository.ReservationRepository;
import com.hyunn.tableplanner.repository.ReviewRepository;
import com.hyunn.tableplanner.repository.StoreRepository;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.service.ReviewService;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ReviewService의 구현체 클래스입니다.
 * 실제 리뷰 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ReservationRepository reservationRepository,
                             StoreRepository storeRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    /**
     * 현재 인증된 사용자를 반환합니다.
     *
     * @return User 현재 인증된 사용자
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));
    }

    /**
     * 리뷰 생성 후 매장의 평점과 리뷰 수를 업데이트합니다.
     *
     * @param store 매장 정보
     * @param newRating 새로운 리뷰 평점
     */
    private void updateStoreRatingAndReviewCountAfterCreate(Store store, int newRating) {
        double totalRating = store.getRating() * store.getReviews();
        int newReviewCount = store.getReviews() + 1;
        double newAverageRating = (totalRating + newRating) / newReviewCount;

        store.setRating(newAverageRating);
        store.setReviews(newReviewCount);

        storeRepository.save(store);
    }

    /**
     * 리뷰 업데이트 후 매장의 평점과 리뷰 수를 업데이트합니다.
     *
     * @param store 매장 정보
     * @param oldRating 기존 리뷰 평점
     * @param newRating 새로운 리뷰 평점
     */
    private void updateStoreRatingAndReviewCountAfterUpdate(Store store, int oldRating, int newRating) {
        double totalRating = store.getRating() * store.getReviews();
        totalRating = totalRating - oldRating + newRating;
        double newAverageRating = totalRating / store.getReviews();

        store.setRating(newAverageRating);

        storeRepository.save(store);
    }

    /**
     * 리뷰 삭제 후 매장의 평점과 리뷰 수를 업데이트합니다.
     *
     * @param store 매장 정보
     * @param oldRating 기존 리뷰 평점
     */
    private void updateStoreRatingAndReviewCountAfterDelete(Store store, int oldRating) {
        double totalRating = store.getRating() * store.getReviews();
        int newReviewCount = store.getReviews() - 1;
        double newAverageRating = newReviewCount > 0 ? (totalRating - oldRating) / newReviewCount : 0.0;

        store.setRating(newAverageRating);
        store.setReviews(newReviewCount);

        storeRepository.save(store);
    }

    /**
     * 리뷰를 생성합니다.
     * 리뷰 생성 시 예약이 완료 상태인지, 이미 리뷰가 작성되었는지 등을 확인합니다.
     *
     * @param request 리뷰 생성 요청 정보
     */
    @Override
    @Transactional
    public void createReview(ReviewCreateRequest request) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        // 예약 ID로 예약 정보 가져오기
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> ReservationException.reservationNotFoundException(request.getReservationId()));
        Long storeId = reservation.getStore().getId();
        Store store = storeRepository.findById(storeId).orElseThrow(() -> StoreException.storeNotFound(storeId));

        // 현재 사용자가 해당 예약의 소유자인지 확인
        if (!reservation.getUser().getUsername().equals(user.getUsername())) {
            throw ReservationException.accessDeniedException(user.getUsername(), request.getReservationId());
        }

        // 예약 상태가 완료 상태인지 확인
        if (!reservation.getStatus().equals(ReservationStatus.COMPLETED)) {
            throw ReviewException.reservationNotCompleted(reservation.getId());
        }

        // 예약에 대해 이미 리뷰가 작성되었는지 확인
        if (reservation.isReviewed()) {
            throw ReviewException.alreadyReviewed(reservation.getId());
        }

        // 리뷰 객체 생성 및 설정
        Review review = new Review();
        review.setUser(user);
        review.setStore(reservation.getStore());
        review.setReservation(reservation);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review); // 리뷰 정보 저장

        // 매장 평점과 리뷰 수 업데이트
        updateStoreRatingAndReviewCountAfterCreate(store, review.getRating());
    }

    /**
     * 리뷰를 업데이트합니다.
     * 리뷰 소유자만 리뷰를 업데이트할 수 있습니다.
     *
     * @param request 리뷰 업데이트 요청 정보
     */
    @Override
    @Transactional
    public void updateReview(ReviewUpdateRequest request) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기
        Long reviewId = request.getReviewId();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> ReviewException.reviewNotFound(reviewId));

        // 현재 사용자가 해당 리뷰의 소유자인지 확인
        if (!review.getUser().getUsername().equals(user.getUsername())) {
            throw ReviewException.unauthorizedAction(user.getUsername(), request.getReviewId());
        }

        int oldRating = review.getRating();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review); // 업데이트된 리뷰 정보 저장

        // 매장 평점과 리뷰 수 업데이트
        updateStoreRatingAndReviewCountAfterUpdate(review.getStore(), oldRating, review.getRating());
    }

    /**
     * 리뷰를 삭제합니다.
     * 리뷰 소유자 또는 매장의 파트너만 리뷰를 삭제할 수 있습니다.
     *
     * @param reviewId 리뷰 ID
     */
    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> ReviewException.reviewNotFound(reviewId));

        // 현재 사용자가 해당 리뷰의 소유자이거나 매장의 파트너인지 확인
        if (!review.getUser().getUsername().equals(user.getUsername()) && !review.getStore()
                .getPartner()
                .getUsername()
                .equals(user.getUsername())) {
            throw ReviewException.unauthorizedAction(user.getUsername(), reviewId);
        }

        reviewRepository.delete(review); // 리뷰 정보 삭제

        // 매장 평점과 리뷰 수 업데이트
        updateStoreRatingAndReviewCountAfterDelete(review.getStore(), review.getRating());
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
    @Override
    public Page<ReviewResponse> getStoreReviews(Long storeId,
                                                Optional<String> sort,
                                                Optional<Integer> minRating,
                                                Optional<Integer> maxRating,
                                                Pageable pageable) {
        // 최소 평점과 최대 평점이 모두 설정된 경우 해당 범위 내의 리뷰를 조회
        if (minRating.isPresent() && maxRating.isPresent()) {
            return reviewRepository.findByStoreIdAndRatingBetween(storeId, minRating.get(), maxRating.get(), pageable)
                    .map(review -> ModelMapperUtil.map(review, ReviewResponse.class));
        }
        // 그렇지 않은 경우 모든 리뷰를 조회
        return reviewRepository.findByStoreId(storeId, pageable)
                .map(review -> ModelMapperUtil.map(review, ReviewResponse.class));
    }

    /**
     * 특정 사용자의 모든 리뷰를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param sort 정렬 기준 (옵션)
     * @param pageable 페이징 정보
     * @return 사용자의 리뷰 목록
     */
    @Override
    public Page<ReviewResponse> getUserReviews(Long userId, Optional<String> sort, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(review -> ModelMapperUtil.map(review, ReviewResponse.class));
    }
}
