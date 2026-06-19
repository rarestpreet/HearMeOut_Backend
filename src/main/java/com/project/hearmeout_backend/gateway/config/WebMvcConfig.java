package com.project.hearmeout_backend.gateway.config;

import com.project.hearmeout_backend.authentication_service.dto.request.RegisterRequestDTO;
import com.project.hearmeout_backend.user_service.mapper.UserMapper;
import com.project.hearmeout_backend.user_service.model.User;
import com.project.hearmeout_backend.authentication_service.model.enums.RoleType;
import com.project.hearmeout_backend.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebMvcConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = getCorsConfig();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        /* cors config requires CorsConfigSources (for global cors config) not
           corsfilter (as it will create two config, one custom and other security config) */
        return source;
    }

    private CorsConfiguration getCorsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration
                .setAllowedOrigins(List.of("http://localhost:5173", "https://hearmeout-frontend.onrender.com"));
        corsConfiguration
                .setAllowedMethods(List.of("PUT", "POST", "GET", "DELETE", "OPTIONS"));
        corsConfiguration
                .setAllowedHeaders(List.of("*"));
        corsConfiguration
                .setMaxAge(3600L);
        corsConfiguration
                .setAllowCredentials(true);

        return corsConfiguration;
    }

    @Bean
    public CommandLineRunner createAdmin(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
        return (args) -> {
            if(!userRepo.existsByUsername("admin")) {
                RegisterRequestDTO dto = new RegisterRequestDTO();
                dto.setUsername("admin");
                dto.setPassword("admin2026");
                dto.setEmail("admin@example.com");

                User user = UserMapper.toProfileEntity(dto, passwordEncoder.encode(dto.getPassword()));
                user.setAccountVerified(true);
                user.getRoles().add(RoleType.ADMIN);

                userRepo.save(user);
            }
        };
    }
}
