package com.yas.backofficebff.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.backofficebff.viewmodel.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthenticationControllerTest {

    private final AuthenticationController authenticationController = new AuthenticationController();

    @Test
    void user_ShouldReturnAuthenticatedUser_WhenPrincipalIsProvided() {
        // Arrange
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("preferred_username")).thenReturn("test_admin");

        // Act
        ResponseEntity<AuthenticatedUser> response = authenticationController.user(principal);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test_admin", response.getBody().username());
    }
}