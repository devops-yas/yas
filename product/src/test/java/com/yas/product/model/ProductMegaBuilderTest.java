package com.yas.product.model;

import org.junit.jupiter.api.Test;
import com.yas.product.model.enumeration.DimensionUnit;
import static org.assertj.core.api.Assertions.assertThat;

class ProductMegaBuilderTest {
    @Test
    void testFullBuilder() {
        // Đã xóa .isVisible(true) để lách qua lỗi biên dịch
        Product p = Product.builder()
            .id(1L)
            .name("name")
            .shortDescription("short")
            .description("desc")
            .specification("spec")
            .sku("sku")
            .gtin("gtin")
            .slug("slug")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(true)
            .stockTrackingEnabled(true)
            .price(100.0)
            .weight(1.0)
            .dimensionUnit(DimensionUnit.CM)
            .length(1.0)
            .width(1.0)
            .height(1.0)
            .taxIncluded(true)
            .brand(new Brand())
            .parent(new Product())
            .build();
            
        assertThat(p).isNotNull();
        assertThat(p.getName()).isEqualTo("name");
    }
}