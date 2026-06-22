package com.project.hearmeout_backend.authentication_service.service.implementation;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import com.project.hearmeout_backend.user_service.dto.response.UserDetailResponseDTO;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@NullMarked
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetailResponseDTO currUser = userRepo
                .findUserForAuth(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found. Enter registered email"
                        )
                );


        return new CustomUserDetails(currUser);
    }
}