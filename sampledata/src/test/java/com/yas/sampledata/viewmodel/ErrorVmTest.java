package com.yas.sampledata.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void shouldCreateDefaultFieldErrorsList() {
        ErrorVm vm = new ErrorVm("400", "Bad Request", "Invalid payload");

        assertEquals("400", vm.statusCode());
        assertEquals("Bad Request", vm.title());
        assertEquals("Invalid payload", vm.detail());
        assertNotNull(vm.fieldErrors());
        assertEquals(List.of(), vm.fieldErrors());
    }
}
