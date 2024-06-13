package com.hyunn.tableplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunn.tableplanner.dto.user.*;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.types.UserRole;
import com.hyunn.tableplanner.security.jwt.JwtTokenProvider;
import com.hyunn.tableplanner.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void register_ValidRequest_ReturnsOk() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "password", "email@example.com");
        doNothing().when(userService).register(any(UserRegisterRequest.class));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).register(any(UserRegisterRequest.class));
    }

    @Test
    void register_DuplicateUsername_ReturnsConflict() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "password", "email@example.com");

        doThrow(UserException.usernameExists("testuser")).when(userService).register(any(UserRegisterRequest.class));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(UserException.usernameExists("testuser").getMessage()));

        verify(userService, times(1)).register(any(UserRegisterRequest.class));
    }

    @Test
    void register_DuplicateEmail_ReturnsConflict() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "password", "email@example.com");

        doThrow(UserException.emailExists("email@example.com")).when(userService)
                .register(any(UserRegisterRequest.class));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(UserException.emailExists("email@example.com").getMessage()));

        verify(userService, times(1)).register(any(UserRegisterRequest.class));
    }

    @WithMockUser
    @Test
    void update_ValidRequest_ReturnsOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("testuser", "newpassword", "newemail@example.com");
        doNothing().when(userService).update(any(UserUpdateRequest.class));

        mockMvc.perform(put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));

        verify(userService, times(1)).update(any(UserUpdateRequest.class));
    }

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void update_DuplicateEmail_ReturnsConflict() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("testuser", "newpassword", "newemail@example.com");
        doThrow(UserException.emailExists("newemail@example.com")).when(userService)
                .update(any(UserUpdateRequest.class));

        mockMvc.perform(put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(UserException.emailExists("newemail@example.com").getMessage()));

        verify(userService, times(1)).update(any(UserUpdateRequest.class));
    }

    @WithMockUser(username = "testuserX", roles = {"USER"})
    @Test
    void update_UnauthorizedRequest_ReturnsUnauthorized() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("testuser", "newpassword", "newemail@example.com");

        doThrow(UserException.unauthorizedException()).when(userService).update(any(UserUpdateRequest.class));

        mockMvc.perform(put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(UserException.unauthorizedException().getMessage()));

        verify(userService, times(1)).update(any(UserUpdateRequest.class));
    }

    @WithMockUser
    @Test
    void delete_ValidRequest_ReturnsOk() throws Exception {
        UserDeleteRequest request = new UserDeleteRequest("testuser");
        doNothing().when(userService).withdraw(any(UserDeleteRequest.class));

        mockMvc.perform(delete("/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService, times(1)).withdraw(any(UserDeleteRequest.class));
    }

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void delete_NonExistentUser_ReturnsNotFound() throws Exception {
        UserDeleteRequest request = new UserDeleteRequest("testuserXX");
        doThrow(UserException.usernameNotFound("testuserXX")).when(userService).withdraw(any(UserDeleteRequest.class));

        mockMvc.perform(delete("/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(UserException.usernameNotFound("testuserXX").getMessage()));

        verify(userService, times(1)).withdraw(any(UserDeleteRequest.class));
    }

    @WithMockUser(username = "testuserX", roles = {"USER"})
    @Test
    void delete_UnauthorizedRequest_ReturnsUnauthorized() throws Exception {
        UserDeleteRequest request = new UserDeleteRequest("testuser");

        doThrow(UserException.unauthorizedException()).when(userService).withdraw(any(UserDeleteRequest.class));

        mockMvc.perform(delete("/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(UserException.unauthorizedException().getMessage()));

        verify(userService, times(1)).withdraw(any(UserDeleteRequest.class));
    }

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void getUserDetails_ValidRequest_ReturnsOk() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String formattedNow = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        UserDetailResponse response = new UserDetailResponse("testuser", "email@example.com", UserRole.USER, now);
        when(userService.findByUsername(anyString())).thenReturn(response);

        mockMvc.perform(get("/users/details/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.createdAt").value(formattedNow));

        verify(userService, times(1)).findByUsername("testuser");
    }

    @WithMockUser(username = "testuserX", roles = {"USER"})
    @Test
    void getUserDetails_UnauthorizedRequest_ReturnsUnauthorized() throws Exception {
        String unauthorizedUser = "testuser";

        doThrow(UserException.unauthorizedException()).when(userService).findByUsername(anyString());

        mockMvc.perform(get("/users/details/" + unauthorizedUser))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(UserException.unauthorizedException().getMessage()));

        verify(userService, times(0)).findByUsername(unauthorizedUser);
    }

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void getUserDetails_NonExistentUser_ReturnsNotFound() throws Exception {
        doThrow(UserException.usernameNotFound("testuser")).when(userService).findByUsername(anyString());

        mockMvc.perform(get("/users/details/testuser"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(UserException.usernameNotFound("testuser").getMessage()));

        verify(userService, times(1)).findByUsername("testuser");
    }

    @WithMockUser
    @Test
    void setPartner_ValidRequest_ReturnsOk() throws Exception {
        UserSetPartnerRequest request = new UserSetPartnerRequest("testuser");
        doNothing().when(userService).setPartner(any(UserSetPartnerRequest.class));

        mockMvc.perform(post("/users/set-partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User set as partner successfully"));

        verify(userService, times(1)).setPartner(any(UserSetPartnerRequest.class));
    }

    @WithMockUser
    @Test
    void setPartner_UserAlreadyPartner_ReturnsConflict() throws Exception {
        UserSetPartnerRequest request = new UserSetPartnerRequest("testuser");
        doThrow(UserException.userAlreadyPartner()).when(userService).setPartner(any(UserSetPartnerRequest.class));

        mockMvc.perform(post("/users/set-partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(UserException.userAlreadyPartner().getMessage()));

        verify(userService, times(1)).setPartner(any(UserSetPartnerRequest.class));
    }

    @WithMockUser(username = "testuserX", roles = {"USER"})
    @Test
    void setPartner_UnauthorizedRequest_ReturnsUnauthorized() throws Exception {
        UserSetPartnerRequest request = new UserSetPartnerRequest("testuser");

        doThrow(UserException.unauthorizedException()).when(userService).setPartner(any(UserSetPartnerRequest.class));

        mockMvc.perform(post("/users/set-partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(UserException.unauthorizedException().getMessage()));

        verify(userService, times(1)).setPartner(any(UserSetPartnerRequest.class));
    }
}
