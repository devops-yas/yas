package com.yas.recommendation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductVariationVmTest {

    @Test
    void shouldExposeValuesAndSupportEquality() {
        Map<Long, String> options = Map.of(1L, "Red");
        ProductVariationVm first = new ProductVariationVm(
                3L,
                "Tee",
                "tee",
                "SKU-1",
                "0123456789",
                19.99,
                options
        );
        ProductVariationVm second = new ProductVariationVm(
                3L,
                "Tee",
                "tee",
                "SKU-1",
                "0123456789",
                19.99,
                options
        );
        ProductVariationVm different = new ProductVariationVm(
                4L,
                "Hoodie",
                "hoodie",
                "SKU-2",
                "9876543210",
                29.99,
                Map.of(2L, "Blue")
        );

        assertEquals(3L, first.id());
        assertEquals("Tee", first.name());
        assertEquals("tee", first.slug());
        assertEquals("SKU-1", first.sku());
        assertEquals("0123456789", first.gtin());
        assertEquals(19.99, first.price());
        assertEquals(options, first.options());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertTrue(first.toString().contains("SKU-1"));
    }
}
