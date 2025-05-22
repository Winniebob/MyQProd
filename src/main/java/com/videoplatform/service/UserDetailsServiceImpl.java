package com.videoplatform.service;

import com.videoplatform.model.User;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // Сначала ищем по email
        User user = userRepository.findByEmail(input)
                .orElseGet(() -> userRepository.findByUsername(input)
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден")));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")  // можно расширить позже
                .build();
    }
}