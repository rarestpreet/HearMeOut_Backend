package com.project.hearmeout_backend.authentication_service.service.implementation;

import com.project.hearmeout_backend.common_lib.exception.InvalidOperationException;
import com.project.hearmeout_backend.common_lib.exception.UserNotFoundException;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import com.project.hearmeout_backend.user_service.service.implementation.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UtilServiceImpl {

    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisOperator;
    private final UserRepository userRepo;
    private final UserServiceImpl userServiceImpl;

    public Integer generateOtp() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }

    @Transactional
    public Integer handlePasswordResetOtp(String email) {
        if(!userRepo.existsByEmail(email)){
            throw new UserNotFoundException("User not found with email: " + email);
        }

        Integer otp = generateOtp();
        redisOperator.opsForValue().set("passresetotp$".concat(email), passwordEncoder.encode(otp.toString()), Duration.ofMinutes(20));
        redisOperator.opsForValue().set("passresetcooldown$".concat(email), "", Duration.ofMinutes(1));

        return otp;
    }

    @Transactional
    public Integer handleAccountVerificationOtp(String email) {
        User user = userServiceImpl.checkAndGetUserByEmail(email);
        if(user.isAccountVerified()){
            throw new InvalidOperationException("User already verified with email: " + email);
        }
        Integer otp = generateOtp();

        redisOperator.opsForValue().set("emailverifyotp$".concat(email), passwordEncoder.encode(otp.toString()), Duration.ofHours(12));
        redisOperator.opsForValue().set("emailverifycooldown$".concat(email), "", Duration.ofMinutes(1));

        return otp;
    }
}
