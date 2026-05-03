package com.yas.tax.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TaxCoverageTest {

    @Test
    void testModels_Coverage() {
        // Quét TaxClass
        com.yas.tax.model.TaxClass taxClass = com.yas.tax.model.TaxClass.builder()
                .id(1L).name("VAT").build();
        taxClass.setName("Standard VAT");
        assertThat(taxClass.getName()).isEqualTo("Standard VAT");

        // Quét TaxRate
        com.yas.tax.model.TaxRate rate = com.yas.tax.model.TaxRate.builder()
                .id(1L).rate(10.0).zipCode("12345").taxClass(taxClass).build();
        rate.setRate(8.0);
        assertThat(rate.getRate()).isEqualTo(8.0);
    }

    @Test
    void testViewModels_Coverage() {
        // Quét TaxClassVm
        com.yas.tax.viewmodel.taxclass.TaxClassVm tc1 = new com.yas.tax.viewmodel.taxclass.TaxClassVm(1L, "VAT");
        com.yas.tax.viewmodel.taxclass.TaxClassVm tc2 = new com.yas.tax.viewmodel.taxclass.TaxClassVm(1L, "VAT");
        assertThat(tc1).isEqualTo(tc2);
        assertThat(tc1.hashCode()).isEqualTo(tc2.hashCode());
        assertThat(tc1.toString()).isNotBlank();

        // Quét TaxRateVm (Chuẩn: Long, Double, String, Long, Long, Long)
        com.yas.tax.viewmodel.taxrate.TaxRateVm tr1 = new com.yas.tax.viewmodel.taxrate.TaxRateVm(1L, 10.0, "123", 1L, 1L, 1L);
        com.yas.tax.viewmodel.taxrate.TaxRateVm tr2 = new com.yas.tax.viewmodel.taxrate.TaxRateVm(1L, 10.0, "123", 1L, 1L, 1L);
        assertThat(tr1).isEqualTo(tr2);
        assertThat(tr1.hashCode()).isEqualTo(tr2.hashCode());

        // Quét TaxRatePostVm (Chuẩn: Double, String, Long, Long, Long)
        com.yas.tax.viewmodel.taxrate.TaxRatePostVm trp1 = new com.yas.tax.viewmodel.taxrate.TaxRatePostVm(10.0, "123", 1L, 1L, 1L);
        com.yas.tax.viewmodel.taxrate.TaxRatePostVm trp2 = new com.yas.tax.viewmodel.taxrate.TaxRatePostVm(10.0, "123", 1L, 1L, 1L);
        assertThat(trp1).isEqualTo(trp2);

        // Quét TaxRateGetDetailVm (Chuẩn: Long, Double, String, String, String, String)
        com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm trgd1 = new com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm(1L, 10.0, "123", "ClassName", "StateName", "CountryName");
        com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm trgd2 = new com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm(1L, 10.0, "123", "ClassName", "StateName", "CountryName");
        assertThat(trgd1).isEqualTo(trgd2);

        // Quét TaxRateListGetVm (Chuẩn: List, int, int, int, int, boolean)
        com.yas.tax.viewmodel.taxrate.TaxRateListGetVm trlg1 = new com.yas.tax.viewmodel.taxrate.TaxRateListGetVm(List.of(trgd1), 1, 10, 1, 1, true);
        com.yas.tax.viewmodel.taxrate.TaxRateListGetVm trlg2 = new com.yas.tax.viewmodel.taxrate.TaxRateListGetVm(List.of(trgd1), 1, 10, 1, 1, true);
        assertThat(trlg1).isEqualTo(trlg2);
    }
}