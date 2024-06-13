package com.hyunn.tableplanner.service.impl;

import com.hyunn.tableplanner.dto.store.*;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.Store;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.repository.StoreRepository;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.service.StoreService;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * StoreService의 구현체 클래스입니다.
 * 실제 매장 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Autowired
    public StoreServiceImpl(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    /**
     * 현재 인증된 사용자를 반환합니다.
     *
     * @return User 현재 인증된 사용자
     */
    private User getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));
    }

    /**
     * ID로 매장을 반환합니다.
     *
     * @param id 매장 ID
     * @return Store 매장 정보
     */
    private Store getStore(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> StoreException.storeNotFound(id));
    }

    /**
     * 주어진 사용자와 매장의 파트너가 일치하는지 확인합니다.
     *
     * @param user  사용자
     * @param store 매장
     */
    private void isStorePartner(User user, Store store) {
        if (!store.getPartner().equals(user)) {
            throw StoreException.unauthorizedException(user.getUsername(), store.getName());
        }
    }

    /**
     * 매장을 등록합니다.
     *
     * @param request 매장 등록 요청 정보
     */
    @Override
    public void registerStore(StoreRegisterRequest request) {
        // 현재 인증된 사용자 가져오기
        User user = getUser();

        // 매장 이름이 이미 존재하는지 확인
        if (storeRepository.existsByName(request.getName())) {
            throw StoreException.storeAlreadyExists(request.getName());
        }

        // 요청 정보를 Store 엔티티로 매핑
        Store store = ModelMapperUtil.map(request, Store.class);
        store.setPartner(user); // 현재 사용자를 파트너로 설정

        // 매장 정보 저장
        storeRepository.save(store);
    }

    /**
     * 매장 정보를 수정합니다.
     *
     * @param request 매장 업데이트 요청 정보
     */
    @Override
    public void updateStore(StoreUpdateRequest request) {
        // 현재 인증된 사용자 가져오기
        User user = getUser();
        // 매장 ID로 매장 정보 가져오기
        Store store = getStore(request.getId());

        // 현재 사용자와 매장의 파트너가 일치하는지 확인
        isStorePartner(user, store);

        // 매장 이름이 변경되었고, 이미 존재하는 이름이면 예외 발생
        if (!store.getName().equals(request.getName()) && storeRepository.existsByName(request.getName())) {
            throw StoreException.storeAlreadyExists(request.getName());
        }

        // 매장 정보 업데이트
        store.setName(request.getName());
        store.setLocation(request.getLocation());
        store.setDescription(request.getDescription());

        // 업데이트된 매장 정보 저장
        storeRepository.save(store);
    }

    /**
     * 매장을 삭제합니다.
     *
     * @param request 매장 삭제 요청 정보
     */
    @Override
    public void withdrawStore(StoreDeleteRequest request) {
        // 현재 인증된 사용자 가져오기
        User user = getUser();
        // 매장 ID로 매장 정보 가져오기
        Store store = getStore(request.getId());

        // 현재 사용자와 매장의 파트너가 일치하는지 확인
        isStorePartner(user, store);

        // 매장 삭제
        storeRepository.delete(store);
    }

    /**
     * 매장 상세 정보를 가져옵니다 (파트너 뷰).
     *
     * @param storeId 매장 ID
     * @return StoreDetailPartnerResponse 매장 상세 정보 (파트너 뷰)
     */
    @Override
    public StoreDetailPartnerResponse getStoreDetailPartner(Long storeId) {
        // 매장 ID로 매장 정보 가져오기
        Store store = getStore(storeId);
        // 현재 인증된 사용자 가져오기
        User user = getUser();
        // 현재 사용자와 매장의 파트너가 일치하는지 확인
        isStorePartner(user, store);

        // 매장 정보를 파트너 뷰 응답 DTO로 매핑
        StoreDetailPartnerResponse response = ModelMapperUtil.map(store, StoreDetailPartnerResponse.class);
        response.setUsername(user.getUsername());

        return response;
    }

    /**
     * 매장 상세 정보를 가져옵니다 (유저 뷰).
     *
     * @param storeId 매장 ID
     * @return StoreDetailUserResponse 매장 상세 정보 (유저 뷰)
     */
    @Override
    public StoreDetailUserResponse getStoreDetailUser(Long storeId) {
        // 매장 ID로 매장 정보 가져오기
        Store store = getStore(storeId);

        // 매장 정보를 유저 뷰 응답 DTO로 매핑
        return ModelMapperUtil.map(store, StoreDetailUserResponse.class);
    }

    /**
     * 모든 매장을 페이지별로 가져옵니다.
     *
     * @param pageable  페이지 요청 정보
     * @param minRating 필터링할 최소 평점
     * @param sortBy    정렬 기준 (rating 또는 reviews)
     * @return Page<StoreSummaryResponse> 페이지별 매장 목록
     */
    @Override
    public Page<StoreSummaryResponse> getAllStores(Pageable pageable, Double minRating, String sortBy) {
        // 정렬 기준 설정
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy.equals("reviews") ? "reviews" : "rating");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 최소 평점 이상인 매장 목록을 페이지별로 가져오기
        return storeRepository.findByRatingGreaterThanEqual(minRating, sortedPageable)
                .map(store -> ModelMapperUtil.map(store, StoreSummaryResponse.class));
    }
}
