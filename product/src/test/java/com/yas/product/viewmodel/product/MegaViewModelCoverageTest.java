package com.yas.product.viewmodel.product;

import com.yas.product.model.enumeration.DimensionUnit;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class MegaViewModelCoverageTest {

    @Test
    void testProductPostVm_Coverage() {
        ProductPostVm vm1 = new ProductPostVm(
                "Name", "slug", 1L, List.of(2L), "Short",
                "Desc", "Spec", "SKU123", "GTIN123",
                10.0, DimensionUnit.CM, 10.0, 10.0, 10.0, 100.0,
                true, true, true, true, true,
                "Meta", "Key", "MetaDesc", 1L,
                null, null, null, null, null, 1L
        );
        ProductPostVm vm2 = new ProductPostVm(
                "Name", "slug", 1L, List.of(2L), "Short",
                "Desc", "Spec", "SKU123", "GTIN123",
                10.0, DimensionUnit.CM, 10.0, 10.0, 10.0, 100.0,
                true, true, true, true, true,
                "Meta", "Key", "MetaDesc", 1L,
                null, null, null, null, null, 1L
        );

        // Ép JaCoCo quét qua 30 trường dữ liệu
        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).isNotBlank();
    }

    @Test
    void testProductPutVm_Coverage() {
        ProductPutVm vm1 = new ProductPutVm(
                "Laptop Pro", "laptop-pro", 1500.0,
                true, true, true, true, true,
                1L, List.of(2L), "Short",
                "Desc", "Spec", "SKU123", "GTIN123",
                10.0, DimensionUnit.CM, 10.0, 10.0, 10.0,
                "Meta", "Key", "MetaDesc", 1L,
                null, null, null, null, null, 1L
        );
        ProductPutVm vm2 = new ProductPutVm(
                "Laptop Pro", "laptop-pro", 1500.0,
                true, true, true, true, true,
                1L, List.of(2L), "Short",
                "Desc", "Spec", "SKU123", "GTIN123",
                10.0, DimensionUnit.CM, 10.0, 10.0, 10.0,
                "Meta", "Key", "MetaDesc", 1L,
                null, null, null, null, null, 1L
        );

        // Ép JaCoCo quét qua 30 trường dữ liệu
        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).isNotBlank();
    }
}