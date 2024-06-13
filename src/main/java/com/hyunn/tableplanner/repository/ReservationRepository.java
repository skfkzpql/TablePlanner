package com.hyunn.tableplanner.repository;

import com.hyunn.tableplanner.model.Reservation;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.model.types.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByConfirmationNumberAndStore_Partner(String confirmationNumber, User partner);

    boolean existsByStore_PartnerIdAndConfirmationNumber(Long partnerId, String confirmationNumber);

    Page<Reservation> findAll(Specification<Reservation> spec, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.status IN :statuses AND r.reservationTime < :threshold")
    List<Reservation> findPendingOrApprovedReservationsBefore(LocalDateTime threshold,
                                                              List<ReservationStatus> statuses);

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = 'OVERDUE' WHERE r.status IN :statuses AND r.reservationTime < :threshold")
    int updateReservationsToOverdue(LocalDateTime threshold, List<ReservationStatus> statuses);
}
