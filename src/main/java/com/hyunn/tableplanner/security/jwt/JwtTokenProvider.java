package com.hyunn.tableplanner.security.jwt;

import com.hyunn.tableplanner.service.impl.UserServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰을 생성하고 검증하는 클래스.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserServiceImpl userServiceImpl;
    private final long tokenValidMilliseconds = 1000L * 60 * 60;

    @Value("${jwt.secret}")
    private String secretKey = "secretKey";
    private SecretKey key;

    /**
     * SecretKey 객체를 초기화.
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 인증 정보를 기반으로 JWT 토큰을 생성한다.
     *
     * @param authentication 인증 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();

        return Jwts.builder()
                .issuer("self")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + tokenValidMilliseconds))
                .subject(authentication.getName())
                .claim("auth", authorities)
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰에서 인증 정보를 가져온다.
     *
     * @param token JWT 토큰
     * @return 인증 정보
     */
    public Authentication getAuthentication(String token) {
        User user = (User) userServiceImpl.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }

    /**
     * JWT 토큰에서 사용자 이름을 추출한다.
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * JWT 토큰의 유효성을 검증한다.
     *
     * @param token JWT 토큰
     * @return 토큰이 유효한 경우 true, 그렇지 않은 경우 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Expired or invalid JWT token");
        }
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출한다.
     *
     * @param request HTTP 요청
     * @return 추출된 JWT 토큰, 없을 경우 null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
