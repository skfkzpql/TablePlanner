package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.store.*;
import com.hyunn.tableplanner.exception.StoreException;
import com.hyunn.tableplanner.exception.UserException;
import com.hyunn.tableplanner.model.Store;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.repository.StoreRepository;
import com.hyunn.tableplanner.repository.UserRepository;
import com.hyunn.tableplanner.util.ModelMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Autowired
    public StoreServiceImpl(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    private User getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.usernameNotFound(username));
    }

    private Store getStore(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> StoreException.storeNotFound(id));
    }

    @Override
    public boolean registerStore(StoreRegisterRequest request) {
        User user = getUser();

        if (storeRepository.existsByName(request.getName())) {
            throw StoreException.storeAlreadyExists(request.getName());
        }

        Store store = ModelMapperUtil.map(request, Store.class);
        store.setPartner(user);

        storeRepository.save(store);
        return true;
    }

    @Override
    public boolean updateStore(StoreUpdateRequest request) {
        User user = getUser();
        Store store = getStore(request.getId());

        if (!store.getPartner().getId().equals(user.getId())) {
            throw StoreException.StoreUpdateUnauthorizedException(user.getUsername(), store.getName());
        }

        store.setName(request.getName());
        store.setLocation(request.getLocation());
        store.setDescription(request.getDescription());

        storeRepository.save(store);
        return true;
    }

    @Override
    public boolean withdrawStore(StoreDeleteRequest request) {
        User user = getUser();
        Store store = getStore(request.getId());

        if (!store.getPartner().getId().equals(user.getId())) {
            throw StoreException.StoreDeleteUnauthorizedException(user.getUsername(), store.getName());
        }

        storeRepository.delete(store);
        return true;
    }

    @Override
    public StoreDetailResponse getStoreDetail(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> StoreException.storeNotFound(id));
        StoreDetailResponse storeDetailResponse = ModelMapperUtil.map(store, StoreDetailResponse.class);
        storeDetailResponse.setUsername(store.getPartner().getUsername());
        return storeDetailResponse;
    }

    @Override
    public List<StoreDTO> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(store -> ModelMapperUtil.map(store, StoreDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreDTO> getStoresSortedByRating() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .sorted(Comparator.comparing(Store::getRating).reversed())
                .map(store -> ModelMapperUtil.map(store, StoreDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreDTO> getStoresSortedByName() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .sorted(Comparator.comparing(Store::getName))
                .map(store -> ModelMapperUtil.map(store, StoreDTO.class))
                .collect(Collectors.toList());
    }
}
