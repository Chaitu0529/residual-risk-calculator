package com.internship.tool.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // A valid Base64-encoded 256-bit key for testing
    private static final String TEST_SECRET =
            "bXktc3VwZXItc2VjcmV0LWtleS1mb3ItdGVzdGluZy11c2Utb25seS0yNTZiaXQ=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    @DisplayName("Should generate a non-null token")
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("testuser");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken("testuser");
        String username = jwtUtil.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate token successfully for matching user")
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when token username does not match")
    void validateToken_ShouldReturnFalse_WhenUsernameMismatch() {
        String token = jwtUtil.generateToken("testuser");
        UserDetails userDetails = new User("otheruser", "password", Collections.emptyList());

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateToken_ShouldReturnFalse_WhenTokenExpired() {
        // Set expiration to -1 ms (already expired)
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String token = jwtUtil.generateToken("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertThat(isValid).isFalse();
    }
}
