package com.hyunn.tableplanner.service.impl;

import com.hyunn.tableplanner.dto.user.*;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.model.types.UserRole;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.service.UserService;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import com.hyunn.tableplanner.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 관리 서비스의 구현체입니다.
 * 실제 비즈니스 로직을 처리합니다.
 */
@Service
public class UserServiceImpl implements UserService {

    // 사용자 Repository
    private final UserRepository userRepository;

    /**
     * UserServiceImpl 생성자
     *
     * @param userRepository UserRepository
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 이름에 해당하는 사용자를 찾고 반환합니다.
     *
     * @param username 사용자 이름
     * @return UserDetails 사용자 상세 정보
     * @throws UsernameNotFoundException 사용자가 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));
        // 스프링 시큐리티 UserDetails 객체로 반환
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    /**
     * 새 사용자를 등록합니다.
     *
     * @param request 사용자 등록 요청 정보
     */
    @Override
    public void register(UserRegisterRequest request) {
        // 사용자 이름이 이미 존재하는지 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw UserException.usernameExists(request.getUsername());
        }

        // 이메일이 이미 존재하는지 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.emailExists(request.getEmail());
        }

        // 사용자 정보를 DTO로 빌드
        UserDTO userDTO = UserDTO.builder()
                .username(request.getUsername())
                .password(PasswordUtil.encryptPassword(request.getPassword())) // 비밀번호 암호화
                .email(request.getEmail())
                .role(UserRole.USER) // 기본 역할을 사용자로 설정
                .build();

        // DTO를 엔티티로 매핑
        User user = ModelMapperUtil.map(userDTO, User.class);

        // 사용자 정보를 데이터베이스에 저장
        userRepository.save(user);
    }

    /**
     * 사용자 정보를 수정합니다.
     *
     * @param request 사용자 정보 수정 요청
     */
    @Override
    public void update(UserUpdateRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자 이름과 요청된 사용자 이름이 다른 경우 예외 발생
        if (!authentication.getName().equals(request.getUsername())) {
            throw UserException.unauthorizedException();
        }

        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        // 이메일이 변경되었고, 이미 존재하는 이메일이면 예외 발생
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw UserException.emailExists(request.getEmail());
        }

        // 비밀번호와 이메일 업데이트
        user.setPassword(PasswordUtil.encryptPassword(request.getNewPassword()));
        user.setEmail(request.getEmail());

        // 업데이트된 사용자 정보 저장
        userRepository.save(user);
    }

    /**
     * 사용자 계정을 삭제합니다.
     *
     * @param request 사용자 삭제 요청
     */
    @Override
    public void withdraw(UserDeleteRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자 이름과 요청된 사용자 이름이 다른 경우 예외 발생
        if (!authentication.getName().equals(request.getUsername())) {
            throw UserException.unauthorizedException();
        }

        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        // 사용자 계정 삭제
        userRepository.delete(user);
    }

    /**
     * 사용자 상세 정보를 반환합니다.
     *
     * @param username 사용자 상세 정보 요청
     * @return UserDetailResponse 사용자 상세 정보
     */
    @Override
    public UserDetailResponse findByUsername(String username) {
        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));

        // 사용자 엔티티를 응답 DTO로 매핑
        return ModelMapperUtil.map(user, UserDetailResponse.class);
    }

    /**
     * 사용자를 파트너로 설정합니다.
     *
     * @param request 파트너 설정 요청
     */
    @Override
    public void setPartner(UserSetPartnerRequest request) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자 이름과 요청된 사용자 이름이 다른 경우 예외 발생
        if (!authentication.getName().equals(request.getUsername())) {
            throw UserException.unauthorizedException();
        }

        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        // 사용자가 이미 파트너인 경우 예외 발생
        if (user.getRole() == UserRole.PARTNER) {
            throw UserException.userAlreadyPartner();
        }

        // 사용자 역할을 파트너로 변경
        user.setRole(UserRole.PARTNER);

        // 업데이트된 사용자 정보 저장
        userRepository.save(user);
    }
}
