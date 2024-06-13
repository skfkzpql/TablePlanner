package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.reservation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * ReservationService 인터페이스입니다.
 * 예약 관련 비즈니스 로직을 정의합니다.
 */
public interface ReservationService {

    /**
     * 예약을 생성합니다.
     *
     * @param request 예약 요청 정보
     * @return 생성된 예약 정보
     */
    ReservationCreateResponse createReservation(ReservationRequest request);

    /**
     * 예약 시간을 업데이트합니다.
     *
     * @param request 예약 업데이트 요청 정보
     * @return 업데이트된 예약 정보
     */
    ReservationUpdateResponse updateReservationTime(ReservationUpdateRequest request);

    /**
     * 예약을 취소합니다.
     *
     * @param request 예약 취소 요청 정보
     */
    void cancelReservation(ReservationCancelRequest request);

    /**
     * 예약을 승인하거나 거절합니다.
     *
     * @param request 예약 승인/거절 요청 정보
     */
    void approveOrRejectReservation(ReservationApprovalRequest request);

    /**
     * 예약을 확인합니다.
     *
     * @param confirmationNumber 예약 확인 번호
     * @return 확인된 예약 정보
     */
    ReservationConfirmResponse confirmReservation(String confirmationNumber);

    /**
     * 예약 상세 정보를 가져옵니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    ReservationResponse getReservationDetail(Long reservationId);

    /**
     * 파트너가 특정 매장의 모든 예약을 조회합니다.
     *
     * @param storeId  매장 ID
     * @param date     날짜 (옵션)
     * @param status   예약 상태 (옵션)
     * @param pageable 페이징 정보
     * @return 예약 목록
     */
    Page<ReservationResponse> getPartnerStoreReservations(Long storeId,
                                                          LocalDate date,
                                                          String status,
                                                          Pageable pageable);

    /**
     * 사용자가 특정 매장의 특정 날짜에 대한 예약 상태를 조회합니다.
     *
     * @param storeId  매장 ID
     * @param date     날짜
     * @param pageable 페이징 정보
     * @return 간단한 예약 정보 목록
     */
    Page<ReservationSimpleResponse> getUserStoreReservations(Long storeId, LocalDate date, Pageable pageable);

    /**
     * 사용자가 자신의 예약 현황을 조회합니다.
     *
     * @param date     날짜 (옵션)
     * @param status   예약 상태 (옵션)
     * @param pageable 페이징 정보
     * @return 예약 정보 목록
     */
    Page<ReservationResponse> getUserReservations(LocalDate date, String status, Pageable pageable);

    /**
     * 예약 상태를 업데이트합니다.
     */
    void updateReservationStatuses();
}
