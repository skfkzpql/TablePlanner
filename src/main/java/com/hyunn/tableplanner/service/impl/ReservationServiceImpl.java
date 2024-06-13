package com.hyunn.tableplanner.service.impl;

import com.hyunn.tableplanner.dto.reservation.*;
import com.hyunn.tableplanner.exception.ReservationException;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.Reservation;
import com.hyunn.tableplanner.model.Store;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.model.types.ReservationStatus;
import com.hyunn.tableplanner.repository.ReservationRepository;
import com.hyunn.tableplanner.repository.StoreRepository;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.service.ReservationService;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ReservationService의 구현체 클래스입니다.
 * 실제 예약 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  UserRepository userRepository,
                                  StoreRepository storeRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
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
     * ID로 매장을 반환합니다.
     *
     * @param id 매장 ID
     * @return Store 매장 정보
     */
    private Store getStoreById(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> StoreException.storeNotFound(id));
    }

    /**
     * ID로 예약을 반환합니다.
     *
     * @param reservationId 예약 ID
     * @return Reservation 예약 정보
     */
    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> ReservationException.reservationNotFoundException(reservationId));
    }

    /**
     * 사용자가 해당 예약의 소유자인지 확인합니다.
     *
     * @param user       사용자
     * @param reservation 예약 정보
     */
    private void isUserReservation(User user, Reservation reservation) {
        if (!Objects.equals(user.getId(), reservation.getUser().getId())) {
            throw ReservationException.accessDeniedException(user.getUsername(), reservation.getId());
        }
    }

    /**
     * 파트너가 해당 예약을 관리할 권한이 있는지 확인합니다.
     *
     * @param partner     파트너
     * @param reservation 예약 정보
     */
    private void isPartnerReservation(User partner, Reservation reservation) {
        if (!Objects.equals(partner.getId(), reservation.getStore().getPartner().getId())) {
            throw ReservationException.accessDeniedException(partner.getUsername(), reservation.getId());
        }
    }

    /**
     * 예약 확인 번호를 생성합니다.
     * 현재 파트너의 예약들 중에서 중복되지 않는 번호를 생성합니다.
     *
     * @return String 예약 확인 번호
     */
    private String generateConfirmationNumber() {
        User partner = getCurrentUser();
        SecureRandom random = new SecureRandom();
        String confirmationNumber;
        do {
            confirmationNumber = String.format("%06d", random.nextInt(999999)) + String.format("%06d", random.nextInt(999999));
        } while (reservationRepository.existsByStore_PartnerIdAndConfirmationNumber(partner.getId(), confirmationNumber));
        return confirmationNumber;
    }

    /**
     * 예약을 생성합니다.
     * 예약 시간의 유효성을 검증한 후 예약을 저장합니다.
     *
     * @param request 예약 요청 정보
     * @return 생성된 예약 정보
     */
    @Override
    public ReservationCreateResponse createReservation(ReservationRequest request) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기
        Store store = getStoreById(request.getStoreId()); // 요청된 매장 ID로 매장 정보 가져오기

        LocalDateTime now = LocalDateTime.now();

        // 예약 시간이 현재 시간으로부터 30분 이내인지 확인
        if (request.getReservationTime().isBefore(now.plusMinutes(30))) {
            throw ReservationException.invalidReservationTimeException("Reservation time cannot be in the past.");
        }

        // 예약 시간이 현재 시간으로부터 2주를 초과하는지 확인
        if (request.getReservationTime().isAfter(now.plusWeeks(2))) {
            throw ReservationException.invalidReservationTimeException("Reservation time cannot be more than 2 weeks from now.");
        }

        // 예약 객체 생성 및 설정
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setStore(store);
        reservation.setReservationTime(request.getReservationTime());
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation); // 예약 정보 저장

        // 생성된 예약 정보를 응답 객체로 매핑하여 반환
        ReservationCreateResponse response = new ReservationCreateResponse();
        response.setStoreName(reservation.getStore().getName());
        response.setReservationTime(reservation.getReservationTime());
        response.setStatus(reservation.getStatus());
        response.setCreatedAt(reservation.getCreatedAt());

        return response;
    }

    /**
     * 예약 시간을 업데이트합니다.
     * 예약 소유자만 예약 시간을 변경할 수 있으며, 새로운 예약 시간의 유효성을 검증합니다.
     *
     * @param request 예약 업데이트 요청 정보
     * @return 업데이트된 예약 정보
     */
    @Override
    public ReservationUpdateResponse updateReservationTime(ReservationUpdateRequest request) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        Reservation reservation = getReservation(request.getId()); // 예약 ID로 예약 정보 가져오기

        isUserReservation(user, reservation); // 현재 사용자가 해당 예약의 소유자인지 확인

        // 예약 상태가 대기 중이거나 승인된 상태인지 확인
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw ReservationException.reservationStatusException(reservation.getStatus());
        }

        LocalDateTime newReservationTime = request.getNewReservationTime();
        LocalDateTime now = LocalDateTime.now();

        // 예외 처리: 현재 시간 이전의 예약 시간은 불가능
        if (newReservationTime.isBefore(now)) {
            throw ReservationException.invalidReservationTimeException("Reservation time cannot be in the past.");
        }

        // 예외 처리: 현재 시간 2주 뒤의 예약은 불가능
        if (newReservationTime.isAfter(now.plusWeeks(2))) {
            throw ReservationException.invalidReservationTimeException("Reservation time cannot be more than 2 weeks from now.");
        }

        // 예외 처리: 10분 이내의 예약 시간으로 변경 불가능
        if (newReservationTime.isBefore(now.plusMinutes(10))) {
            throw ReservationException.invalidReservationTimeException("Reservation time cannot be within the next 10 minutes.");
        }

        // 예약 시간 및 상태 업데이트
        reservation.setReservationTime(newReservationTime);
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation); // 업데이트된 예약 정보 저장

        // 업데이트된 예약 정보를 응답 객체로 매핑하여 반환
        ReservationUpdateResponse response = ModelMapperUtil.map(reservation, ReservationUpdateResponse.class);
        response.setStoreName(reservation.getStore().getName());

        return response;
    }

    /**
     * 예약을 취소합니다.
     * 예약 소유자만 예약을 취소할 수 있으며, 예약 상태를 취소됨으로 변경합니다.
     *
     * @param request 예약 취소 요청 정보
     */
    @Override
    public void cancelReservation(ReservationCancelRequest request) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        Reservation reservation = getReservation(request.getReservationId()); // 예약 ID로 예약 정보 가져오기

        isUserReservation(user, reservation); // 현재 사용자가 해당 예약의 소유자인지 확인

        // 예약 상태가 대기 중이거나 승인된 상태인지 확인
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw ReservationException.reservationStatusException(reservation.getStatus());
        }

        // 예약 상태를 취소됨으로 변경
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation); // 업데이트된 예약 정보 저장
    }

    /**
     * 예약을 승인하거나 거절합니다.
     * 파트너만 예약을 승인하거나 거절할 수 있습니다.
     *
     * @param request 예약 승인/거절 요청 정보
     */
    @Override
    public void approveOrRejectReservation(ReservationApprovalRequest request) {
        User partner = getCurrentUser(); // 현재 인증된 파트너 가져오기

        Reservation reservation = getReservation(request.getReservationId()); // 예약 ID로 예약 정보 가져오기
        ReservationStatus requestStatus = ReservationStatus.valueOf(request.getStatus()); // 요청된 상태로 변환

        isPartnerReservation(partner, reservation); // 현재 파트너가 해당 예약을 관리할 권한이 있는지 확인

        // 예약 상태가 대기 중인지 확인
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ReservationException.reservationStatusException(reservation.getStatus());
        }

        // 예약 상태 업데이트
        if (requestStatus == ReservationStatus.APPROVED) {
            reservation.setStatus(ReservationStatus.APPROVED);
            reservation.setConfirmationNumber(generateConfirmationNumber()); // 확인 번호 생성 및 설정
        } else if (requestStatus == ReservationStatus.REJECTED) {
            reservation.setStatus(ReservationStatus.REJECTED);
        } else {
            throw ReservationException.reservationStatusException(requestStatus);
        }

        reservation.setUpdatedAt(LocalDateTime.now()); // 예약 정보 업데이트 시간 설정

        reservationRepository.save(reservation); // 업데이트된 예약 정보 저장
    }

    /**
     * 예약을 확인합니다.
     * 파트너가 예약 확인 번호를 통해 예약을 확인합니다.
     *
     * @param confirmationNumber 예약 확인 번호
     * @return 확인된 예약 정보
     */
    @Override
    public ReservationConfirmResponse confirmReservation(String confirmationNumber) {
        User partner = getCurrentUser(); // 현재 인증된 파트너 가져오기

        // 예약 확인 번호와 파트너로 예약을 조회
        Reservation reservation = reservationRepository.findByConfirmationNumberAndStore_Partner(confirmationNumber, partner)
                .orElseThrow(() -> ReservationException.confirmationNumberNotFoundException(confirmationNumber));

        reservation.setStatus(ReservationStatus.COMPLETED); // 예약 상태를 완료로 변경
        reservationRepository.save(reservation); // 업데이트된 예약 정보 저장

        // 확인된 예약 정보를 응답 객체로 매핑하여 반환
        ReservationConfirmResponse response = ModelMapperUtil.map(reservation, ReservationConfirmResponse.class);
        response.setUserId(reservation.getUser().getId());
        response.setStoreId(reservation.getStore().getId());

        return response;
    }

    /**
     * 예약 상세 정보를 가져옵니다.
     * 예약 소유자와 파트너만 접근할 수 있습니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    public ReservationResponse getReservationDetail(Long reservationId) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        Reservation reservation = getReservation(reservationId); // 예약 ID로 예약 정보 가져오기

        Store store = reservation.getStore();

        // 현재 사용자가 해당 예약의 사용자이거나 매장의 파트너인지 확인
        if (!reservation.getUser().getId().equals(user.getId()) && !store.getPartner().getId().equals(user.getId())) {
            throw ReservationException.accessDeniedException(user.getUsername(), reservation.getId());
        }

        // 예약 정보를 응답 객체로 매핑하여 반환
        ReservationResponse response = ModelMapperUtil.map(reservation, ReservationResponse.class);
        response.setUserId(reservation.getUser().getId());
        response.setStoreId(reservation.getStore().getId());

        return response;
    }

    /**
     * 파트너가 특정 매장의 모든 예약을 조회합니다.
     *
     * @param storeId  매장 ID
     * @param date     날짜 (옵션)
     * @param status   예약 상태 (옵션)
     * @param pageable 페이징 정보
     * @return 예약 목록
     */
    @Override
    public Page<ReservationResponse> getPartnerStoreReservations(Long storeId,
                                                                 LocalDate date,
                                                                 String status,
                                                                 Pageable pageable) {
        User partner = getCurrentUser(); // 현재 인증된 파트너 가져오기

        Store store = storeRepository.findById(storeId).orElseThrow(() -> StoreException.storeNotFound(storeId));

        // 현재 파트너가 해당 매장의 파트너인지 확인
        if (!partner.equals(store.getPartner())) {
            throw StoreException.unauthorizedException(partner.getUsername(), store.getName());
        }

        // 예약 조건을 생성하는 사양을 설정
        Specification<Reservation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("store"), store));
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("reservationTime"), startOfDay, endOfDay));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), ReservationStatus.valueOf(status)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 설정된 조건으로 예약을 페이지 단위로 조회하여 반환
        Page<Reservation> reservations = reservationRepository.findAll(spec, pageable);
        return reservations.map(reservation -> ModelMapperUtil.map(reservation, ReservationResponse.class));
    }

    /**
     * 사용자가 특정 매장의 특정 날짜에 대한 예약 상태를 조회합니다.
     *
     * @param storeId  매장 ID
     * @param date     날짜
     * @param pageable 페이징 정보
     * @return 간단한 예약 정보 목록
     */
    @Override
    public Page<ReservationSimpleResponse> getUserStoreReservations(Long storeId, LocalDate date, Pageable pageable) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> StoreException.storeNotFound(storeId));

        // 예약 조건을 생성하는 사양을 설정
        Specification<Reservation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("store"), store));
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("reservationTime"), startOfDay, endOfDay));
            }
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("status"), ReservationStatus.PENDING),
                    criteriaBuilder.equal(root.get("status"), ReservationStatus.APPROVED)
            ));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 설정된 조건으로 예약을 페이지 단위로 조회하여 반환
        Page<Reservation> reservations = reservationRepository.findAll(spec, pageable);
        return reservations.map(reservation -> ModelMapperUtil.map(reservation, ReservationSimpleResponse.class));
    }

    /**
     * 사용자가 자신의 예약 현황을 조회합니다.
     *
     * @param date     날짜 (옵션)
     * @param status   예약 상태 (옵션)
     * @param pageable 페이징 정보
     * @return 예약 정보 목록
     */
    @Override
    public Page<ReservationResponse> getUserReservations(LocalDate date, String status, Pageable pageable) {
        User user = getCurrentUser(); // 현재 인증된 사용자 가져오기

        // 예약 조건을 생성하는 사양을 설정
        Specification<Reservation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user"), user));
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("reservationTime"), startOfDay, endOfDay));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), ReservationStatus.valueOf(status)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 설정된 조건으로 예약을 페이지 단위로 조회하여 반환
        Page<Reservation> reservations = reservationRepository.findAll(spec, pageable);
        return reservations.map(reservation -> ModelMapperUtil.map(reservation, ReservationResponse.class));
    }

    /**
     * 예약 상태를 업데이트합니다.
     * 예약 상태를 주기적으로 확인하여 만료된 예약을 업데이트합니다.
     */
    @Override
    @Scheduled(fixedRate = 60000) // 1분 간격으로 예약 상태를 업데이트
    @Transactional
    public void updateReservationStatuses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(10);

        List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);

        int updatedCount = reservationRepository.updateReservationsToOverdue(threshold, statuses);

        System.out.println("Updated " + updatedCount + " reservations to OVERDUE status.");
    }
}
