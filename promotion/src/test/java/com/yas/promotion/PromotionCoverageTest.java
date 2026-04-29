package com.yas.promotion;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import com.yas.promotion.model.enumeration.ApplyTo;
import com.yas.promotion.model.enumeration.DiscountType;
import com.yas.promotion.model.enumeration.UsageType;
import com.yas.promotion.viewmodel.PromotionVerifyResultDto;
import com.yas.promotion.viewmodel.PromotionVerifyVm;

import java.util.List;

class PromotionCoverageTest {

    @Test
    void testEnums() {
        // Quét các hàm values() và valueOf() ẩn của Enum
        assertThat(ApplyTo.values()).isNotEmpty();
        assertThat(ApplyTo.valueOf("PRODUCT")).isEqualTo(ApplyTo.PRODUCT);

        assertThat(DiscountType.values()).isNotEmpty();
        assertThat(DiscountType.valueOf("FIXED")).isEqualTo(DiscountType.FIXED);

        assertThat(UsageType.values()).isNotEmpty();
        assertThat(UsageType.valueOf("LIMITED")).isEqualTo(UsageType.LIMITED);
    }

    @Test
    void testRecords() {
        // Quét các hàm equals, hashCode, toString ẩn của Record
        PromotionVerifyResultDto dto1 = new PromotionVerifyResultDto(true, 1L, "code", DiscountType.FIXED, 100L);
        PromotionVerifyResultDto dto2 = new PromotionVerifyResultDto(true, 1L, "code", DiscountType.FIXED, 100L);
        
        assertThat(dto1.isValid()).isTrue();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).isNotBlank();

        PromotionVerifyVm vm1 = new PromotionVerifyVm("code", 100L, List.of(1L));
        PromotionVerifyVm vm2 = new PromotionVerifyVm("code", 100L, List.of(1L));
        
        assertThat(vm1.couponCode()).isEqualTo("code");
        assertThat(vm1).isEqualTo(vm2);
        assertThat(vm1.hashCode()).isEqualTo(vm2.hashCode());
        assertThat(vm1.toString()).isNotBlank();
    }
}