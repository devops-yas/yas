package com.yas.search.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductEsDetailVmTest {

    @Test
    void testProductEsDetailVm_withAllData() {
        // Arrange
        Long id = 1L;
        String name = "Test Product";
        String slug = "test-product";
        Double price = 99.99;
        boolean isPublished = true;
        boolean isVisibleIndividually = true;
        boolean isAllowedToOrder = false;
        boolean isFeatured = true;
        Long thumbnailMediaId = 100L;
        String brand = "TestBrand";
        List<String> categories = new ArrayList<>();
        categories.add("Electronics");
        List<String> attributes = new ArrayList<>();
        attributes.add("Color=Red");

        // Act
        ProductEsDetailVm vm = new ProductEsDetailVm(
            id, name, slug, price, isPublished, isVisibleIndividually,
            isAllowedToOrder, isFeatured, thumbnailMediaId, brand, categories, attributes
        );

        // Assert
        assertEquals(id, vm.id());
        assertEquals(name, vm.name());
        assertEquals(slug, vm.slug());
        assertEquals(price, vm.price());
        assertEquals(isPublished, vm.isPublished());
        assertEquals(isVisibleIndividually, vm.isVisibleIndividually());
        assertEquals(isAllowedToOrder, vm.isAllowedToOrder());
        assertEquals(isFeatured, vm.isFeatured());
        assertEquals(thumbnailMediaId, vm.thumbnailMediaId());
        assertEquals(brand, vm.brand());
        assertEquals(1, vm.categories().size());
        assertEquals("Electronics", vm.categories().get(0));
        assertEquals(1, vm.attributes().size());
        assertEquals("Color=Red", vm.attributes().get(0));
    }
}
