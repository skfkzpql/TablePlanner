package com.hyunn.tableplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyunn.tableplanner.dto.reservation.*;
import com.hyunn.tableplanner.exception.ReservationException;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.model.types.ReservationStatus;
import com.hyunn.tableplanner.security.jwt.JwtTokenProvider;
import com.hyunn.tableplanner.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ReservationService reservationService;


    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    public ReservationControllerTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser
    void createReservation_ValidRequest_ReturnsOk() throws Exception {
        ReservationRequest request = new ReservationRequest(1L, LocalDateTime.now().plusDays(1));
        ReservationCreateResponse response = new ReservationCreateResponse("Test Store", LocalDateTime.now(),
                ReservationStatus.PENDING,
                LocalDateTime.now());

        when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Test Store"));
    }

    @Test
    @WithMockUser
    void createReservation_InvalidRequest_ThrowsException() throws Exception {
        ReservationRequest request = new ReservationRequest(1L, LocalDateTime.now().minusDays(1));

        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(ReservationException.invalidReservationTimeException(
                        "Reservation time cannot be in the past."));

        mockMvc.perform(MockMvcRequestBuilders.post("/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Reservation time cannot be in the past."));
    }

    @Test
    @WithMockUser
    void updateReservation_ValidRequest_ReturnsOk() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(1L, LocalDateTime.now().plusDays(1));
        ReservationUpdateResponse response = new ReservationUpdateResponse("Test Store", LocalDateTime.now(),
                ReservationStatus.PENDING,
                LocalDateTime.now(), LocalDateTime.now());

        when(reservationService.updateReservationTime(any(ReservationUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Test Store"));
    }

    @Test
    @WithMockUser
    void updateReservation_InvalidRequest_ThrowsException() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(1L, LocalDateTime.now().minusDays(1));

        when(reservationService.updateReservationTime(any(ReservationUpdateRequest.class)))
                .thenThrow(ReservationException.invalidReservationTimeException(
                        "Reservation time cannot be in the past."));

        mockMvc.perform(MockMvcRequestBuilders.put("/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Reservation time cannot be in the past."));
    }

    @Test
    @WithMockUser
    void cancelReservation_ValidRequest_ReturnsOk() throws Exception {
        ReservationCancelRequest request = new ReservationCancelRequest(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/reservations/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void cancelReservation_InvalidRequest_ThrowsException() throws Exception {
        ReservationCancelRequest request = new ReservationCancelRequest(1L);

        Mockito.doThrow(ReservationException.reservationNotFoundException(1L))
                .when(reservationService).cancelReservation(any(ReservationCancelRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/reservations/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Reservation with ID 1 not found."));
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void approveOrRejectReservation_ValidRequest_ReturnsOk() throws Exception {
        ReservationApprovalRequest request = new ReservationApprovalRequest(1L, "APPROVED");

        mockMvc.perform(MockMvcRequestBuilders.put("/reservations/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void approveOrRejectReservation_InvalidRequest_ThrowsException() throws Exception {
        ReservationApprovalRequest request = new ReservationApprovalRequest(1L, "APPROVED");

        Mockito.doThrow(ReservationException.reservationNotFoundException(1L))
                .when(reservationService).approveOrRejectReservation(any(ReservationApprovalRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/reservations/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Reservation with ID 1 not found."));
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void confirmReservation_ValidRequest_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ReservationConfirmResponse response = new ReservationConfirmResponse(1L, 1L, now.plusDays(1),
                ReservationStatus.COMPLETED, now, now, "123456");

        when(reservationService.confirmReservation("123456")).thenReturn(response);

        mockMvc.perform(get("/reservations/confirm/123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void confirmReservation_InvalidRequest_ThrowsException() throws Exception {
        when(reservationService.confirmReservation("123456"))
                .thenThrow(ReservationException.confirmationNumberNotFoundException("123456"));

        mockMvc.perform(get("/reservations/confirm/123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Reservation with confirmation number 123456 not found."));
    }

    @Test
    @WithMockUser
    void getReservationDetail_ValidRequest_ReturnsOk() throws Exception {
        ReservationResponse response = new ReservationResponse(1L, 1L, LocalDateTime.now(), ReservationStatus.PENDING,
                "2024-06-10T08:00:00", "2024-06-10T08:00:00", "123456", false);

        when(reservationService.getReservationDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/reservations/detail/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void getReservationDetail_InvalidRequest_ThrowsException() throws Exception {
        when(reservationService.getReservationDetail(1L))
                .thenThrow(ReservationException.reservationNotFoundException(1L));

        mockMvc.perform(get("/reservations/detail/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Reservation with ID 1 not found."));
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void getPartnerStoreReservations_ValidRequest_ReturnsOk() throws Exception {
        Page<ReservationResponse> reservations = Page.empty();

        when(reservationService.getPartnerStoreReservations(1L,
                null,
                null,
                Pageable.unpaged())).thenReturn(reservations);

        mockMvc.perform(get("/reservations/store/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void getPartnerStoreReservations_InvalidRequest_ThrowsException() throws Exception {
        when(reservationService.getPartnerStoreReservations(Mockito.anyLong(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(Pageable.class)))
                .thenThrow(StoreException.storeNotFound(1L));

        mockMvc.perform(MockMvcRequestBuilders.get("/reservations/store/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StoreException.storeNotFound(1L).getMessage()));
    }

    @Test
    void getUserStoreReservations_ValidRequest_ReturnsOk() throws Exception {
        Page<ReservationSimpleResponse> reservations = Page.empty();

        when(reservationService.getUserStoreReservations(1L, LocalDate.now(), Pageable.unpaged())).thenReturn(
                reservations);

        mockMvc.perform(get("/reservations/user/store/1/" + LocalDate.now())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    void getUserReservations_ValidRequest_ReturnsOk() throws Exception {
        Page<ReservationResponse> reservations = Page.empty();

        when(reservationService.getUserReservations(null, null, Pageable.unpaged())).thenReturn(reservations);

        mockMvc.perform(get("/reservations/user/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
