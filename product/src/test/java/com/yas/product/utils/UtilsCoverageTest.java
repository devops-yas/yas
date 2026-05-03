package com.yas.product.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UtilsCoverageTest {

    @Test
    void testConstantsInstantiationAndAccess() {
        Constants constants = new Constants();
        // Khởi tạo inner class từ instance của class cha
        Constants.ErrorCode errorCode = constants.new ErrorCode();
        
        assertThat(constants).isNotNull();
        assertThat(errorCode).isNotNull();
        
        assertThat(Constants.ErrorCode.PRODUCT_NOT_FOUND).isNotBlank();
    }
}