package com.yas.search.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ProductNameGetVmTest {

    @Test
    void testProductNameGetVm_withValidName() {
        // Arrange
        String name = "Test Product";

        // Act
        ProductNameGetVm vm = new ProductNameGetVm(name);

        // Assert
        assertEquals(name, vm.name());
    }

    @Test
    void testProductNameGetVm_withEmptyName() {
        // Arrange
        String name = "";

        // Act
        ProductNameGetVm vm = new ProductNameGetVm(name);

        // Assert
        assertEquals("", vm.name());
    }
}
