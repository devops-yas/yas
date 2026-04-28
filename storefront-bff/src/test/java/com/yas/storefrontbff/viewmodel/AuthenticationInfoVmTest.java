package com.yas.storefrontbff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthenticationInfoVmTest {

    @Test
    void shouldRepresentUnauthenticatedState() {
        AuthenticationInfoVm vm = new AuthenticationInfoVm(false, null);

        assertFalse(vm.isAuthenticated());
        assertNull(vm.authenticatedUser());
    }

    @Test
    void shouldRepresentAuthenticatedStateAndValueSemantics() {
        AuthenticatedUserVm user = new AuthenticatedUserVm("alice");
        AuthenticationInfoVm vm = new AuthenticationInfoVm(true, user);
        AuthenticationInfoVm same = new AuthenticationInfoVm(true, new AuthenticatedUserVm("alice"));
        AuthenticationInfoVm different = new AuthenticationInfoVm(true, new AuthenticatedUserVm("bob"));

        assertTrue(vm.isAuthenticated());
        assertEquals("alice", vm.authenticatedUser().username());
        assertEquals(vm, same);
        assertEquals(vm.hashCode(), same.hashCode());
        assertNotEquals(vm, different);
    }
}
