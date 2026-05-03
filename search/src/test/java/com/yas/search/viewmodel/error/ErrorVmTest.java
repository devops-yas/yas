package com.yas.search.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testErrorVm_withAllFields() {
        // Arrange
        String statusCode = "400";
        String title = "Bad Request";
        String detail = "Invalid parameters";

        // Act
        ErrorVm vm = new ErrorVm(statusCode, title, detail);

        // Assert
        assertEquals(statusCode, vm.statusCode());
        assertEquals(title, vm.title());
        assertEquals(detail, vm.detail());
        assertNotNull(vm.fieldErrors());
        assertEquals(0, vm.fieldErrors().size());
    }

    @Test
    void testErrorVm_defaultConstructor() {
        // Act
        ErrorVm vm = new ErrorVm("500", "Server Error", "Internal error");

        // Assert
        assertEquals("500", vm.statusCode());
        assertEquals("Server Error", vm.title());
        assertEquals("Internal error", vm.detail());
        assertNotNull(vm.fieldErrors());
        assertEquals(0, vm.fieldErrors().size());
    }
}
