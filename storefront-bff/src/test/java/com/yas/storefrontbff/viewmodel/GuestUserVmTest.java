package com.yas.storefrontbff.viewmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestUserVmTest {

    @Test
    void constructorShouldCreateGuestUserVm() {
        GuestUserVm guestUserVm = new GuestUserVm(
            "user-001",
            "guest@example.com",
            "password123"
        );

        assertEquals("user-001", guestUserVm.userId());
        assertEquals("guest@example.com", guestUserVm.email());
        assertEquals("password123", guestUserVm.password());
    }
}
