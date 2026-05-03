package com.yas.product.viewmodel.product;

import org.junit.jupiter.api.Test;
import com.yas.product.model.enumeration.DimensionUnit;
import static org.assertj.core.api.Assertions.assertThat;

class ProductDetailVmTest {

    @Test
    void testRecordAndBuilder() {
        // Phủ hàm Builder do Lombok sinh ra cho Record
        ProductDetailVm vm = ProductDetailVm.builder()
            .id(1L)
            .name("Test VM")
            .price(99.99)
            .dimensionUnit(DimensionUnit.CM)
            .build();

        // Phủ các accessor method của Record
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.name()).isEqualTo("Test VM");
        assertThat(vm.price()).isEqualTo(99.99);
        assertThat(vm.dimensionUnit()).isEqualTo(DimensionUnit.CM);

        // Phủ tính năng toBuilder()
        ProductDetailVm updatedVm = vm.toBuilder()
            .name("Updated VM")
            .build();

        assertThat(updatedVm.id()).isEqualTo(1L);
        assertThat(updatedVm.name()).isEqualTo("Updated VM");
    }
}