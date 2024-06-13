package com.hyunn.tableplanner.repository;

import com.hyunn.tableplanner.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    Page<Review> findByStoreId(Long storeId, Pageable pageable);

    Page<Review> findByStoreIdAndRatingBetween(Long storeId, int minRating, int maxRating, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);
}
