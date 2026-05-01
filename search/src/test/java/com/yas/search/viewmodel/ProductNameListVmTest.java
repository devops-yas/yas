package com.yas.search.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductNameListVmTest {

    @Test
    void testProductNameListVm_withValidData() {
        // Arrange
        List<ProductNameGetVm> productNames = new ArrayList<>();
        productNames.add(new ProductNameGetVm("Product1"));
        productNames.add(new ProductNameGetVm("Product2"));

        // Act
        ProductNameListVm vm = new ProductNameListVm(productNames);

        // Assert
        assertNotNull(vm.productNames());
        assertEquals(2, vm.productNames().size());
        assertEquals("Product1", vm.productNames().get(0).name());
        assertEquals("Product2", vm.productNames().get(1).name());
    }

    @Test
    void testProductNameListVm_withEmptyList() {
        // Arrange
        List<ProductNameGetVm> productNames = new ArrayList<>();

        // Act
        ProductNameListVm vm = new ProductNameListVm(productNames);

        // Assert
        assertNotNull(vm.productNames());
        assertEquals(0, vm.productNames().size());
    }
}
