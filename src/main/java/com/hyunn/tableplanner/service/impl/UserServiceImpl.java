package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.user.*;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.model.types.UserRole;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import com.hyunn.tableplanner.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 사용자 관리 서비스의 구현체
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    /**
     * 새 사용자를 등록합니다.
     *
     * @param request 사용자 등록 요청 정보
     * @return boolean 등록 성공 여부
     */
    @Override
    public boolean register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw UserException.usernameExists(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.emailExists(request.getEmail());
        }
        UserDTO userDTO = UserDTO.builder()
                .username(request.getUsername())
                .password(PasswordUtil.encryptPassword(request.getPassword()))
                .email(request.getEmail())
                .role(UserRole.USER)
                .build();

        User user = ModelMapperUtil.map(userDTO, User.class);

        userRepository.save(user);
        return true;
    }

    /**
     * 사용자 정보를 수정합니다.
     *
     * @param request 사용자 정보 수정 요청
     * @return boolean 수정 성공 여부
     */
    @Override
    public boolean update(UserUpdateRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        user.setPassword(PasswordUtil.encryptPassword(request.getNewPassword()));
        user.setEmail(request.getEmail());
        userRepository.save(user);
        return true;
    }

    /**
     * 사용자 계정을 삭제합니다.
     *
     * @param request 사용자 삭제 요청
     * @return boolean 삭제 성공 여부
     */
    @Override
    public boolean withdraw(UserDeleteRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        userRepository.delete(user);
        return true;
    }

    /**
     * 사용자 상세 정보를 반환합니다.
     *
     * @param username 사용자 상세 정보 요청
     * @return UserDTO 사용자 상세 정보
     */
    @Override
    public UserDetailResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));

        return ModelMapperUtil.map(user, UserDetailResponse.class);
    }

    /**
     * 사용자를 파트너로 설정합니다.
     *
     * @param request 파트너 설정 요청
     * @return boolean 파트너 설정 성공 여부
     */
    @Override
    public boolean setPartner(UserSetPartnerRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> UserException.usernameNotFound(request.getUsername()));

        if (user.getRole() == UserRole.PARTNER) {
            throw UserException.userAlreadyPartner();
        }
        user.setRole(UserRole.PARTNER);
        userRepository.save(user);
        return true;
    }
}
