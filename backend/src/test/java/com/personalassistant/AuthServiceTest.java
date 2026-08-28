package com.personalassistant;

import com.personalassistant.auth.AuthResponse;
import com.personalassistant.auth.AuthService;
import com.personalassistant.auth.LoginRequest;
import com.personalassistant.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void testRegisterAndLogin() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password("StrongPass123!")
                .currency("INR")
                .timezone("Asia/Kolkata")
                .build();

        AuthResponse regResponse = authService.register(registerReq);
        assertNotNull(regResponse.getAccessToken());
        assertEquals("jane.doe@example.com", regResponse.getUser().getEmail());

        LoginRequest loginReq = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("StrongPass123!")
                .build();

        AuthResponse loginResponse = authService.login(loginReq);
        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());
    }
}
