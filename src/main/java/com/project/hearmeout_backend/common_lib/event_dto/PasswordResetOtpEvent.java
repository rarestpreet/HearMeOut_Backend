package com.project.hearmeout_backend.common_lib.event_dto;

public record PasswordResetOtpEvent(String email, Integer otp) {}
