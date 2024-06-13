package com.hyunn.tableplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyunn.tableplanner.dto.review.ReviewCreateRequest;
import com.hyunn.tableplanner.dto.review.ReviewResponse;
import com.hyunn.tableplanner.dto.review.ReviewUpdateRequest;
import com.hyunn.tableplanner.exception.ReservationException;
import com.hyunn.tableplanner.exception.ReviewException;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.security.jwt.JwtTokenProvider;
import com.hyunn.tableplanner.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser
    void createReview_ValidRequest_ReturnsOk() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(1L, 5, "Great!");
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Review created successfully"));
    }

    @Test
    @WithMockUser
    void createReview_InvalidRequest_ThrowsException() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(1L, 5, "Great!");
        Mockito.doThrow(ReservationException.accessDeniedException("testuser", 1L))
                .when(reviewService).createReview(any(ReviewCreateRequest.class));

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(ReservationException.accessDeniedException("testuser", 1L).getMessage()));
    }

    @Test
    @WithMockUser
    void updateReview_ValidRequest_ReturnsOk() throws Exception {
        ReviewUpdateRequest request = new ReviewUpdateRequest(1L, 5, "Updated review");
        mockMvc.perform(put("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Review updated successfully"));
    }

    @Test
    @WithMockUser
    void updateReview_InvalidRequest_ThrowsException() throws Exception {
        ReviewUpdateRequest request = new ReviewUpdateRequest(1L, 5, "Updated review");
        Mockito.doThrow(ReviewException.reviewNotFound(1L))
                .when(reviewService).updateReview(any(ReviewUpdateRequest.class));

        mockMvc.perform(put("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Review with ID 1 not found."));
    }

    @Test
    @WithMockUser
    void deleteReview_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully"));
    }

    @Test
    @WithMockUser
    void deleteReview_InvalidRequest_ThrowsException() throws Exception {
        Mockito.doThrow(ReviewException.reviewNotFound(1L))
                .when(reviewService).deleteReview(anyLong());

        mockMvc.perform(delete("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Review with ID 1 not found."));
    }

    @Test
    void getStoreReviews_ValidRequest_ReturnsOk() throws Exception {
        Page<ReviewResponse> reviews = new PageImpl<>(Collections.emptyList());
        Mockito.when(reviewService.getStoreReviews(anyLong(),
                        any(Optional.class),
                        any(Optional.class),
                        any(Optional.class),
                        any(Pageable.class)))
                .thenReturn(reviews);

        mockMvc.perform(get("/reviews/store/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getStoreReviews_InvalidRequest_ThrowsException() throws Exception {
        Mockito.doThrow(StoreException.storeNotFound(1L))
                .when(reviewService).getStoreReviews(anyLong(),
                        any(Optional.class),
                        any(Optional.class),
                        any(Optional.class),
                        any(Pageable.class));

        mockMvc.perform(get("/reviews/store/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StoreException.storeNotFound(1L).getMessage()));
    }

    @Test
    @WithMockUser
    void getUserReviews_ValidRequest_ReturnsOk() throws Exception {
        Page<ReviewResponse> reviews = new PageImpl<>(Collections.emptyList());
        Mockito.when(reviewService.getUserReviews(anyLong(), any(Optional.class), any(Pageable.class)))
                .thenReturn(reviews);

        mockMvc.perform(get("/reviews/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserReviews_InvalidRequest_ThrowsException() throws Exception {
        Mockito.doThrow(UserException.usernameNotFound("testuser"))
                .when(reviewService).getUserReviews(anyLong(), any(Optional.class), any(Pageable.class));

        mockMvc.perform(get("/reviews/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(UserException.usernameNotFound("testuser").getMessage()));
    }
}
