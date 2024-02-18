package org.example.config.security;

import org.example.exception.notfound.UserNotFoundException;
import org.example.exception.security.BlockedRequestException;
import org.example.repository.UserRepository;
import org.example.service.LoginAttemptService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private final LoginAttemptService loginAttemptService;

    public UserDetailsServiceImpl(UserRepository userRepository, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        if (loginAttemptService.isBlocked()) {
            throw new BlockedRequestException("blocked");
        }

        return userRepository
                .findByUsername(username)
                .map(
                        storedUser -> org.springframework.security.core.userdetails.User.builder()
                                .username(storedUser.getUsername())
                                .password(storedUser.getPassword())
                                .accountExpired(!storedUser.isActive())
                                .accountLocked(false)
                                .build()
                )
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
    }
}
