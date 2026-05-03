package com.yas.storefrontbff.viewmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenResponseVmTest {

    @Test
    void constructorShouldCreateTokenResponseVm() {
        TokenResponseVm tokenResponseVm = new TokenResponseVm(
            "access-token-value",
            "refresh-token-value"
        );

        assertEquals("access-token-value", tokenResponseVm.accessToken());
        assertEquals("refresh-token-value", tokenResponseVm.refreshToken());
    }
}
