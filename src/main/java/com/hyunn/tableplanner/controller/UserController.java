package com.hyunn.tableplanner.controller;

import com.hyunn.tableplanner.dto.user.*;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 엔드포인트를 관리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     *
     * @param userRegisterRequest 사용자 등록 요청 정보
     * @return 회원가입 성공 여부
     */
    @PermitAll
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        userService.register(userRegisterRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * 사용자 정보 업데이트
     *
     * @param userUpdateRequest 사용자 업데이트 요청 정보
     * @return 업데이트 성공 여부
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    public ResponseEntity<String> update(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.update(userUpdateRequest);
        return ResponseEntity.ok("User updated successfully");
    }

    /**
     * 계정 삭제
     *
     * @param userDeleteRequest 사용자 삭제 요청 정보
     * @return 삭제 성공 여부
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@Valid @RequestBody UserDeleteRequest userDeleteRequest) {
        userService.withdraw(userDeleteRequest);
        return ResponseEntity.ok("User deleted successfully");
    }

    /**
     * 사용자 상세 정보 조회
     *
     * @param username 사용자 이름
     * @return 사용자 상세 정보
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/details/{username}")
    public ResponseEntity<?> getUserDetails(@PathVariable String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.getName().equals(username)) {
            throw UserException.unauthorizedException();
        }

        UserDetailResponse userDetailResponse = userService.findByUsername(username);
        return ResponseEntity.ok(userDetailResponse);
    }

    /**
     * 사용자를 파트너로 설정
     *
     * @param request 파트너 설정 요청 정보
     * @return 파트너 설정 성공 여부
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/set-partner")
    public ResponseEntity<String> setPartner(@Valid @RequestBody UserSetPartnerRequest request) {
        userService.setPartner(request);
        return ResponseEntity.ok("User set as partner successfully");
    }
}
