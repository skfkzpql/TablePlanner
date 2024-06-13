package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.user.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 서비스 인터페이스입니다.
 * 사용자 관련 비즈니스 로직을 정의합니다.
 */
@Service
public interface UserService extends UserDetailsService {

    /**
     * 사용자 이름으로 사용자 상세 정보를 불러옴
     *
     * @param username 사용자 이름
     * @return UserDetails 사용자 상세 정보
     * @throws UsernameNotFoundException 사용자가 없을 경우 발생
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * 새 사용자를 등록
     *
     * @param request 사용자 등록 요청 정보
     */
    void register(UserRegisterRequest request);

    /**
     * 사용자 정보를 업데이트
     *
     * @param request 사용자 업데이트 요청 정보
     */
    void update(UserUpdateRequest request);

    /**
     * 사용자 계정을 삭제
     *
     * @param request 사용자 삭제 요청 정보
     */
    void withdraw(UserDeleteRequest request);

    /**
     * 사용자 상세 정보를 가져옴
     *
     * @param username 사용자 이름
     * @return UserDetailResponse 사용자 상세 정보
     */
    UserDetailResponse findByUsername(String username);

    /**
     * 사용자를 파트너로 설정
     *
     * @param request 파트너 설정 요청 정보
     */
    void setPartner(UserSetPartnerRequest request);
}
