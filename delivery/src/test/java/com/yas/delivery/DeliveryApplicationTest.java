package com.yas.delivery;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeliveryApplicationTest {

    @Test
    void contextLoads() {
        // Kiểm tra xem Spring Application Context có khởi tạo thành công không
    }

    @Test
    void main() {
        // Dùng mockStatic để vét coverage hàm main mà không khởi động lại server
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            DeliveryApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(DeliveryApplication.class, new String[]{}));
        }
    }
}