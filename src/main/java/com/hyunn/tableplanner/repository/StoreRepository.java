package com.hyunn.tableplanner.repository;

import com.hyunn.tableplanner.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByName(String name);

    @Query("SELECT s FROM Store s WHERE s.rating >= :minRating")
    Page<Store> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);
}
