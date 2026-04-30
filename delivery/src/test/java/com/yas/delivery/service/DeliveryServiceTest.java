package com.yas.delivery.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DeliveryServiceTest {

    private final DeliveryService deliveryService = new DeliveryService();

    @Test
    void testServiceInitialization() {
        // Đảm bảo Service có thể khởi tạo thành công (chuẩn bị cho logic sau này)
        assertNotNull(deliveryService);
    }
}