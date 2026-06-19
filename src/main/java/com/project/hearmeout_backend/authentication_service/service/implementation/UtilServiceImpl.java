package com.project.hearmeout_backend.authentication_service.service.implementation;

import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UtilServiceImpl {

    private final UserServiceImpl userServiceImpl;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public Integer generateOtp() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }

    @Transactional
    public Integer handlePasswordResetOtp(String email) {
        User registeredUser = userServiceImpl.checkAndGetUserByEmail(email);

        Integer otp = generateOtp();
        Long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);

        registeredUser.setPasswordChangeOtp(passwordEncoder.encode(otp.toString()));
        registeredUser.setPasswordOtpExpireAt(expirationTime);

        userRepo.save(registeredUser);

        return otp;
    }

    @Transactional
    public Integer handleAccountVerificationOtp(String email) {
        User registeredUser = userServiceImpl.checkAndGetUserByEmail(email);

        Integer otp = generateOtp();
        Long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);

        registeredUser.setEmailVerifyOtp(passwordEncoder.encode(otp.toString()));
        registeredUser.setEmailVerifyOtpExpireAt(expirationTime);

        userRepo.save(registeredUser);

        return otp;
    }
}
