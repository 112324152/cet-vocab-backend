package com.cetvocab.backend.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cetvocab.backend.mapper.UserMapper;
import com.cetvocab.backend.model.UserRow;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRow row = userMapper.findByUsername(username);
        if (row == null) {
            throw new UsernameNotFoundException("user not found: " + username);
        }
        return User.builder()
                .username(row.getUsername())
                .password(row.getPasswordHash())
                .roles("USER")
                .build();
    }
}
