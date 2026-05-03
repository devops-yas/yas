package com.yas.product.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void testGettersAndSetters() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPrice(100.0);
        product.setPublished(true);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getSlug()).isEqualTo("test-product");
        assertThat(product.getPrice()).isEqualTo(100.0);
        assertThat(product.isPublished()).isTrue();
    }

    @Test
    void testBuilder() {
        Product product = Product.builder()
            .id(2L)
            .name("Builder Product")
            .sku("SKU123")
            .build();

        assertThat(product.getId()).isEqualTo(2L);
        assertThat(product.getName()).isEqualTo("Builder Product");
        assertThat(product.getSku()).isEqualTo("SKU123");
    }

    @Test
    void testEqualsAndHashCode() {
        Product product1 = new Product();
        product1.setId(100L);

        Product product2 = new Product();
        product2.setId(100L);

        Product product3 = new Product();
        product3.setId(200L);

        // Phủ sạch các nhánh if/else bên trong hàm equals() mà bạn đã override
        assertThat(product1).isEqualTo(product1);
        assertThat(product1).isEqualTo(product2);
        assertThat(product1).isNotEqualTo(product3);
        assertThat(product1).isNotEqualTo(new Object());
        assertThat(product1).isNotEqualTo(null);

        // Phủ hàm hashCode()
        assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
    }
}