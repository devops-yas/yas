package com.yas.storefrontbff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class AuthenticatedUserVmTest {

    @Test
    void shouldExposeUsernameAndSupportValueEquality() {
        AuthenticatedUserVm vm = new AuthenticatedUserVm("alice");
        AuthenticatedUserVm same = new AuthenticatedUserVm("alice");
        AuthenticatedUserVm different = new AuthenticatedUserVm("bob");

        assertEquals("alice", vm.username());
        assertEquals(vm, same);
        assertEquals(vm.hashCode(), same.hashCode());
        assertNotEquals(vm, different);
    }
}
