package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yas.commonlibrary.constants.ApiConstant;
import com.yas.commonlibrary.exception.AccessDeniedException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractUserIdReturnsJwtSubject() {
        Jwt jwt = jwt();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertEquals("user-123", AuthenticationUtils.extractUserId());
    }

    @Test
    void extractJwtReturnsTokenValue() {
        Jwt jwt = jwt();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertEquals("token-value", AuthenticationUtils.extractJwt());
    }

    @Test
    void extractUserIdThrowsAccessDeniedForAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);

        assertEquals(ApiConstant.ACCESS_DENIED, exception.getMessage());
    }

    private Jwt jwt() {
        return new Jwt(
            "token-value",
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T01:00:00Z"),
            Map.of("alg", "none"),
            Map.of("sub", "user-123")
        );
    }
}
