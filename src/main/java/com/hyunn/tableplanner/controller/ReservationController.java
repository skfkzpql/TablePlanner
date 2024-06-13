package com.hyunn.tableplanner.controller;

import com.hyunn.tableplanner.dto.reservation.*;
import com.hyunn.tableplanner.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 예약 관련 엔드포인트를 관리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약을 생성합니다.
     *
     * @param request 예약 요청 정보
     * @return 생성된 예약 정보
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<ReservationCreateResponse> createReservation(@RequestBody ReservationRequest request) {
        ReservationCreateResponse response = reservationService.createReservation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 시간을 업데이트합니다.
     *
     * @param request 예약 업데이트 요청 정보
     * @return 업데이트된 예약 정보
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    public ResponseEntity<ReservationUpdateResponse> updateReservation(@RequestBody ReservationUpdateRequest request) {
        ReservationUpdateResponse response = reservationService.updateReservationTime(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 예약을 취소합니다.
     *
     * @param request 예약 취소 요청 정보
     * @return 취소 성공 여부
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelReservation(@RequestBody ReservationCancelRequest request) {
        reservationService.cancelReservation(request);
        return ResponseEntity.ok("Reservation cancelled successfully");
    }

    /**
     * 예약을 승인하거나 거절합니다.
     *
     * @param request 예약 승인/거절 요청 정보
     * @return 승인/거절 성공 여부
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/approve")
    public ResponseEntity<String> approveOrRejectReservation(@RequestBody ReservationApprovalRequest request) {
        reservationService.approveOrRejectReservation(request);
        return ResponseEntity.ok("Reservation approved/rejected successfully");
    }

    /**
     * 예약을 확인합니다.
     * 파트너 계정으로 로그인 되어 있는 키오스크에서 예약 확인 번호를 입력하여 예약을 확인합니다.
     *
     * @param confirmationNumber 예약 확인 번호
     * @return 확인된 예약 정보
     */
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/confirm/{confirmationNumber}")
    public ResponseEntity<ReservationConfirmResponse> confirmReservation(@PathVariable String confirmationNumber) {
        ReservationConfirmResponse response = reservationService.confirmReservation(confirmationNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 상세 정보를 가져옵니다. (파트너와 해당 예약 사용자만 접근 가능)
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationDetail(@PathVariable Long reservationId) {
        ReservationResponse response = reservationService.getReservationDetail(reservationId);
        return ResponseEntity.ok(response);
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
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ReservationResponse>> getPartnerStoreReservations(
            @PathVariable Long storeId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<ReservationResponse> reservations = reservationService.getPartnerStoreReservations(storeId,
                date,
                status,
                pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 사용자가 특정 매장의 특정 날짜에 대한 예약 상태를 조회합니다.
     *
     * @param storeId  매장 ID
     * @param date     날짜
     * @param pageable 페이징 정보
     * @return 간단한 예약 정보 목록
     */
    @PreAuthorize("permitAll()")
    @GetMapping("/user/store/{storeId}/{date}")
    public ResponseEntity<Page<ReservationSimpleResponse>> getUserStoreReservations(
            @PathVariable Long storeId,
            @PathVariable LocalDate date,
            Pageable pageable) {
        Page<ReservationSimpleResponse> reservations = reservationService.getUserStoreReservations(storeId,
                date,
                pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 사용자가 자신의 예약 현황을 조회합니다.
     *
     * @param date     날짜 (옵션)
     * @param status   예약 상태 (옵션)
     * @param pageable 페이징 정보
     * @return 예약 정보 목록
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/reservations")
    public ResponseEntity<Page<ReservationResponse>> getUserReservations(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<ReservationResponse> reservations = reservationService.getUserReservations(date, status, pageable);
        return ResponseEntity.ok(reservations);
    }
}
