package com.yas.product.viewmodel.product;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RemainingViewModelCoverageTest {

    @Test
    void testVariousViewModels() {
        // 1. Quét ProductListVm
        ProductListVm pl1 = new ProductListVm(1L, "name", "slug", true, true, true, true, 10.0, null, 1L, 1L);
        ProductListVm pl2 = new ProductListVm(1L, "name", "slug", true, true, true, true, 10.0, null, 1L, 1L);
        assertThat(pl1).isEqualTo(pl2);
        assertThat(pl1.hashCode()).isEqualTo(pl2.hashCode());
        assertThat(pl1.toString()).isNotBlank();

        // 2. Quét ProductQuantityPutVm & ProductQuantityPostVm
        ProductQuantityPutVm pq1 = new ProductQuantityPutVm(1L, 10L);
        ProductQuantityPutVm pq2 = new ProductQuantityPutVm(1L, 10L);
        assertThat(pq1).isEqualTo(pq2);
        assertThat(pq1.hashCode()).isEqualTo(pq2.hashCode());

        ProductQuantityPostVm pqp1 = new ProductQuantityPostVm(1L, 10L);
        ProductQuantityPostVm pqp2 = new ProductQuantityPostVm(1L, 10L);
        assertThat(pqp1).isEqualTo(pqp2);

        // 3. Quét ProductGetCheckoutListVm
        ProductGetCheckoutListVm pgcl1 = new ProductGetCheckoutListVm(List.of(), 1, 1, 1, 1, true);
        ProductGetCheckoutListVm pgcl2 = new ProductGetCheckoutListVm(List.of(), 1, 1, 1, 1, true);
        assertThat(pgcl1).isEqualTo(pgcl2);

        // 4. Quét ProductCheckoutListVm (Đã bổ sung đủ 14 tham số)
        ProductCheckoutListVm pcl1 = new ProductCheckoutListVm(
                1L, "name", "slug", "sku", "gtin", 1L, 1L, 10.0, 1L, "url", null, "creator", null, "updater"
        );
        ProductCheckoutListVm pcl2 = new ProductCheckoutListVm(
                1L, "name", "slug", "sku", "gtin", 1L, 1L, 10.0, 1L, "url", null, "creator", null, "updater"
        );
        assertThat(pcl1).isEqualTo(pcl2);
        
        // 5. Quét ProductDetailGetVm
        ProductDetailGetVm pdg1 = new ProductDetailGetVm(1L, "Name", "brand", List.of(), List.of(), "short", "desc", "spec", true, true, true, true, 10.0, "thumb", List.of());
        ProductDetailGetVm pdg2 = new ProductDetailGetVm(1L, "Name", "brand", List.of(), List.of(), "short", "desc", "spec", true, true, true, true, 10.0, "thumb", List.of());
        assertThat(pdg1).isEqualTo(pdg2);
    }
}