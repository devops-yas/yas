package com.yas.backofficebff.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUp() {
        clientRegistrationRepository = mock(ReactiveClientRegistrationRepository.class);
        securityConfig = new SecurityConfig(clientRegistrationRepository);
    }

    @Test
    void userAuthoritiesMapperForKeycloak_WithOidcUserAuthority() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();

        // Tạo dữ liệu thật (claims) để tránh lỗi empty attributes của Spring Security
        Map<String, Object> claims = Map.of(
            "sub", "test-user",
            "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        );

        OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.now().plusSeconds(3600), claims);
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        OidcUserAuthority authority = new OidcUserAuthority("ROLE_USER", idToken, userInfo);

        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(Set.of(authority));

        assertEquals(2, mappedAuthorities.size());
        assertTrue(mappedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(mappedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void userAuthoritiesMapperForKeycloak_WithOAuth2UserAuthority() {
        GrantedAuthoritiesMapper mapper = securityConfig.userAuthoritiesMapperForKeycloak();

        Map<String, Object> attributes = Map.of(
            "sub", "test-user",
            "realm_access", Map.of("roles", List.of("SUPER_ADMIN"))
        );
        OAuth2UserAuthority authority = new OAuth2UserAuthority(attributes);

        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(Set.of(authority));

        assertEquals(1, mappedAuthorities.size());
        assertTrue(mappedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
    }
}