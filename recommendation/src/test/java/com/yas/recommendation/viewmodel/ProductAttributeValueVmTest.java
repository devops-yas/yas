package com.yas.recommendation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProductAttributeValueVmTest {

    @Test
    void shouldExposeValuesAndSupportEquality() {
        ProductAttributeValueVm first = new ProductAttributeValueVm(1L, "Color", "Blue");
        ProductAttributeValueVm second = new ProductAttributeValueVm(1L, "Color", "Blue");
        ProductAttributeValueVm different = new ProductAttributeValueVm(2L, "Size", "M");

        assertEquals(1L, first.id());
        assertEquals("Color", first.nameProductAttribute());
        assertEquals("Blue", first.value());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertTrue(first.toString().contains("Color"));
    }
}
