package com.hyunn.tableplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunn.tableplanner.dto.store.*;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.security.jwt.JwtTokenProvider;
import com.hyunn.tableplanner.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void registerStore_ValidRequest_ReturnsOk() throws Exception {
        StoreRegisterRequest request = new StoreRegisterRequest("Test Store", "123 Main St", "Test description");

        mockMvc.perform(post("/stores/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Store registered successfully"));

        verify(storeService).registerStore(any(StoreRegisterRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void registerStore_DuplicateName_ReturnsConflict() throws Exception {
        StoreRegisterRequest request = new StoreRegisterRequest("Test Store", "123 Main St", "Test description");

        doThrow(StoreException.storeAlreadyExists("Test Store")).when(storeService).registerStore(any(
                StoreRegisterRequest.class));

        mockMvc.perform(post("/stores/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(StoreException.storeAlreadyExists("Test Store").getMessage()));

        verify(storeService).registerStore(any(StoreRegisterRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void updateStore_ValidRequest_ReturnsOk() throws Exception {
        StoreUpdateRequest request = new StoreUpdateRequest(1L, "Updated Store", "456 Main St", "Updated description");

        mockMvc.perform(put("/stores/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Store updated successfully"));

        verify(storeService).updateStore(any(StoreUpdateRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void updateStore_DuplicateName_ReturnsConflict() throws Exception {
        StoreUpdateRequest request = new StoreUpdateRequest(1L,
                "Duplicate Store",
                "456 Main St",
                "Updated description");

        doThrow(StoreException.storeAlreadyExists("Duplicate Store")).when(storeService).updateStore(any(
                StoreUpdateRequest.class));

        mockMvc.perform(put("/stores/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(StoreException.storeAlreadyExists("Duplicate Store").getMessage()));

        verify(storeService).updateStore(any(StoreUpdateRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void updateStore_NotAuthorized_ReturnsForbidden() throws Exception {
        StoreUpdateRequest request = new StoreUpdateRequest(1L, "Updated Store", "456 Main St", "Updated description");

        doThrow(StoreException.unauthorizedException("testuser", "Updated Store")).when(storeService).updateStore(any(
                StoreUpdateRequest.class));

        mockMvc.perform(put("/stores/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(StoreException.unauthorizedException("testuser", "Updated Store")
                        .getMessage()));

        verify(storeService).updateStore(any(StoreUpdateRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void withdrawStore_ValidRequest_ReturnsOk() throws Exception {
        StoreDeleteRequest request = new StoreDeleteRequest(1L);

        mockMvc.perform(delete("/stores/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Store withdrawn successfully"));

        verify(storeService).withdrawStore(any(StoreDeleteRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void withdrawStore_NotAuthorized_ReturnsForbidden() throws Exception {
        StoreDeleteRequest request = new StoreDeleteRequest(1L);

        doThrow(StoreException.unauthorizedException("testuser", "Test Store")).when(storeService).withdrawStore(any(
                StoreDeleteRequest.class));

        mockMvc.perform(delete("/stores/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(StoreException.unauthorizedException("testuser", "Test Store")
                        .getMessage()));

        verify(storeService).withdrawStore(any(StoreDeleteRequest.class));
    }

    @WithMockUser
    @Test
    void getStoreDetailUser_ValidRequest_ReturnsOk() throws Exception {
        Long storeId = 1L;
        StoreDetailUserResponse response = new StoreDetailUserResponse(storeId,
                "Test Store",
                "123 Main St",
                "Test description",
                4.5,
                10);

        when(storeService.getStoreDetailUser(storeId)).thenReturn(response);

        mockMvc.perform(get("/stores/detail/user/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storeId))
                .andExpect(jsonPath("$.name").value("Test Store"))
                .andExpect(jsonPath("$.location").value("123 Main St"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.reviews").value(10));

        verify(storeService).getStoreDetailUser(storeId);
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void getStoreDetailPartner_ValidRequest_ReturnsOk() throws Exception {
        Long storeId = 1L;
        StoreDetailPartnerResponse response = new StoreDetailPartnerResponse(storeId,
                "testuser",
                "Test Store",
                "123 Main St",
                "Test description",
                4.5,
                10,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(storeService.getStoreDetailPartner(storeId)).thenReturn(response);

        mockMvc.perform(get("/stores/detail/partner/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storeId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test Store"))
                .andExpect(jsonPath("$.location").value("123 Main St"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.reviews").value(10));

        verify(storeService).getStoreDetailPartner(storeId);
    }

    @WithMockUser
    @Test
    void getAllStores_ValidRequest_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<StoreSummaryResponse> storeSummaryResponses = new ArrayList<>();
        storeSummaryResponses.add(new StoreSummaryResponse(1L, "Test Store", 4.5));
        Page<StoreSummaryResponse> response = new PageImpl<>(storeSummaryResponses,
                pageable,
                storeSummaryResponses.size());

        when(storeService.getAllStores(any(Pageable.class), any(Double.class), any(String.class))).thenReturn(response);

        mockMvc.perform(get("/stores")
                        .param("page", "0")
                        .param("size", "10")
                        .param("minRating", "0")
                        .param("sortBy", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Store"))
                .andExpect(jsonPath("$.content[0].rating").value(4.5));

        verify(storeService).getAllStores(any(Pageable.class), any(Double.class), any(String.class));
    }

    // 예외 상황 테스트 추가

    @WithMockUser(roles = "PARTNER")
    @Test
    void registerStore_InvalidInput_ReturnsBadRequest() throws Exception {
        StoreRegisterRequest request = new StoreRegisterRequest("", "123 Main St", "Test description");

        mockMvc.perform(post("/stores/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storeService, times(0)).registerStore(any(StoreRegisterRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void updateStore_InvalidInput_ReturnsBadRequest() throws Exception {
        StoreUpdateRequest request = new StoreUpdateRequest(1L, "", "456 Main St", "Updated description");

        mockMvc.perform(put("/stores/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storeService, times(0)).updateStore(any(StoreUpdateRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void withdrawStore_InvalidInput_ReturnsBadRequest() throws Exception {
        StoreDeleteRequest request = new StoreDeleteRequest(null);

        mockMvc.perform(delete("/stores/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storeService, times(0)).withdrawStore(any(StoreDeleteRequest.class));
    }

    @WithMockUser(roles = "PARTNER")
    @Test
    void getStoreDetailPartner_NotFound_ReturnsNotFound() throws Exception {
        Long storeId = 999L;

        doThrow(StoreException.storeNotFound(storeId)).when(storeService).getStoreDetailPartner(storeId);

        mockMvc.perform(get("/stores/detail/partner/{storeId}", storeId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StoreException.storeNotFound(storeId).getMessage()));

        verify(storeService).getStoreDetailPartner(storeId);
    }

    @WithMockUser
    @Test
    void getStoreDetailUser_NotFound_ReturnsNotFound() throws Exception {
        Long storeId = 999L;

        doThrow(StoreException.storeNotFound(storeId)).when(storeService).getStoreDetailUser(storeId);

        mockMvc.perform(get("/stores/detail/user/{storeId}", storeId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StoreException.storeNotFound(storeId).getMessage()));

        verify(storeService).getStoreDetailUser(storeId);
    }
}

