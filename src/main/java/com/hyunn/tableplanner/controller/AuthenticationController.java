package com.hyunn.tableplanner.controller;

import com.hyunn.tableplanner.dto.auth.LoginRequest;
import com.hyunn.tableplanner.dto.auth.LoginResponse;
import com.hyunn.tableplanner.security.jwt.JwtTokenProvider;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 엔드포인트를 관리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인
     *
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 성공 시 JWT 토큰 반환
     */
    @PermitAll
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 사용자 인증을 시도합니다.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 인증에 성공하면 JWT 토큰을 생성합니다.
            String token = jwtTokenProvider.generateToken(authentication);
            LoginResponse loginResponse = new LoginResponse(token);
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            // 인증에 실패하면 HTTP 상태 코드 401과 함께 null 토큰을 반환합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null));
        }
    }
}
