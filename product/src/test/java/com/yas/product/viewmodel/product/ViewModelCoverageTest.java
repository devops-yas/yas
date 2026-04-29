package com.yas.product.viewmodel.product;

import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.productattribute.ProductAttributeGroupGetVm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ViewModelCoverageTest {

    @Test
    void testProductVariationPutVm_Coverage() {
        ProductVariationPutVm vm1 = new ProductVariationPutVm(
                1L, "Variant 1", "slug-1", "SKU1", "GTIN1",
                100.0, 10L, List.of(100L, 101L), Map.of(1L, "Value1")
        );

        ProductVariationPutVm vm2 = new ProductVariationPutVm(
                1L, "Variant 1", "slug-1", "SKU1", "GTIN1",
                100.0, 10L, List.of(100L, 101L), Map.of(1L, "Value1")
        );

        assertThat(vm1.id()).isEqualTo(1L);
        assertThat(vm1.name()).isEqualTo("Variant 1");
        assertThat(vm1.price()).isEqualTo(100.0);

        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).contains("Variant 1");
    }

    @Test
    void testProductVariationGetVm_Coverage() {
        ImageVm image = new ImageVm(1L, "http://image.url");
        
        ProductVariationGetVm vm1 = new ProductVariationGetVm(
                1L, "Variant Get", "slug-get", "SKU-GET", "GTIN-GET",
                200.0, image, List.of(image), Map.of(1L, "Option1")
        );

        ProductVariationGetVm vm2 = new ProductVariationGetVm(
                1L, "Variant Get", "slug-get", "SKU-GET", "GTIN-GET",
                200.0, image, List.of(image), Map.of(1L, "Option1")
        );

        assertThat(vm1.id()).isEqualTo(1L);
        assertThat(vm1.name()).isEqualTo("Variant Get");
        assertThat(vm1.thumbnail()).isNotNull();

        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).isNotNull();
    }

    @Test
    void testProductDetailGetVm_Coverage() {
        ProductDetailGetVm vm1 = new ProductDetailGetVm(
                1L, "Detail Get", "Brand Name", List.of("Category 1"),
                List.of(new ProductAttributeGroupGetVm("Group", List.of())),
                "Short Desc", "Desc", "Spec", true, true, true, true,
                500.0, "http://thumb.url", List.of("http://img1.url")
        );

        ProductDetailGetVm vm2 = new ProductDetailGetVm(
                1L, "Detail Get", "Brand Name", List.of("Category 1"),
                List.of(new ProductAttributeGroupGetVm("Group", List.of())),
                "Short Desc", "Desc", "Spec", true, true, true, true,
                500.0, "http://thumb.url", List.of("http://img1.url")
        );

        assertThat(vm1.id()).isEqualTo(1L);
        assertThat(vm1.brandName()).isEqualTo("Brand Name");
        
        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).contains("Detail Get");
    }
}