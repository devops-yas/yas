package com.yas.storefrontbff.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.storefrontbff.viewmodel.AuthenticationInfoVm;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AuthenticationControllerTest {

    private final AuthenticationController controller = new AuthenticationController();

    @Test
    void shouldReturnUnauthenticatedWhenPrincipalIsNull() {
        ResponseEntity<AuthenticationInfoVm> response = controller.user(null);

        assertNotNull(response.getBody());
        assertFalse(response.getBody().isAuthenticated());
        assertNull(response.getBody().authenticatedUser());
    }

    @Test
    void shouldReturnAuthenticatedUserWhenPrincipalExists() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("preferred_username")).thenReturn("alice");

        ResponseEntity<AuthenticationInfoVm> response = controller.user(principal);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isAuthenticated());
        assertNotNull(response.getBody().authenticatedUser());
        assertEquals("alice", response.getBody().authenticatedUser().username());
    }
}
