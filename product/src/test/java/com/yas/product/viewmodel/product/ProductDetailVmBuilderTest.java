package com.yas.product.viewmodel.product;

import org.junit.jupiter.api.Test;
import com.yas.product.model.enumeration.DimensionUnit;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ProductDetailVmBuilderTest {
    @Test
    void testBuilderAndToBuilder() {
        ProductDetailVm vm = ProductDetailVm.builder()
            .id(1L).name("n").shortDescription("sd").description("d").specification("s")
            .sku("sku").gtin("gtin").slug("slug").isAllowedToOrder(true).isPublished(true)
            .isFeatured(true).isVisible(true).stockTrackingEnabled(true).weight(1.0)
            .dimensionUnit(DimensionUnit.CM).length(1.0).width(1.0).height(1.0)
            .price(10.0).brandId(1L).categories(List.of()).metaTitle("mt")
            .metaKeyword("mk").metaDescription("md").thumbnailMedia(null)
            .productImageMedias(List.of()).taxClassId(1L).parentId(1L)
            .build();
        
        // Gọi toBuilder() - Lệnh này ép hệ thống phải copy từng field một, ăn cực kì nhiều điểm!
        ProductDetailVm vm2 = vm.toBuilder().name("new name").build();
        
        assertThat(vm2).isNotNull();
        assertThat(vm2.name()).isEqualTo("new name");
    }
}