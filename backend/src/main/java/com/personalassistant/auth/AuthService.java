package com.personalassistant.auth;

import com.personalassistant.common.BadRequestException;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserDto;
import com.personalassistant.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email address is already in use", "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .currency(request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency().toUpperCase().trim() : "INR")
                .timezone(request.getTimezone() != null && !request.getTimezone().isBlank() ? request.getTimezone().trim() : "Asia/Kolkata")
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = tokenProvider.generateTokenFromUserId(
                savedUser.getId(), savedUser.getEmail(), savedUser.getName()
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(UserDto.fromEntity(savedUser))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(UserDto.fromEntity(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateTokenFromUserId(user.getId(), user.getEmail(), user.getName());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                    return AuthResponse.builder()
                            .accessToken(token)
                            .refreshToken(newRefreshToken.getToken())
                            .user(UserDto.fromEntity(user))
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(userId).ifPresent(refreshTokenService::deleteByUserId);
    }
}
