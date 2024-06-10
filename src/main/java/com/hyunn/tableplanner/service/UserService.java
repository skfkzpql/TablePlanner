package com.hyunn.tableplanner.service;

import com.hyunn.tableplanner.dto.UserDTO;
import com.hyunn.tableplanner.model.User;
import com.hyunn.tableplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    boolean register(UserDTO userDTO);

    boolean update(UserDTO userDTO);

    boolean withdraw(String username);

    UserDTO getUserDetails(String username);

}
