package com.yas.delivery.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DeliveryControllerTest {

    private final DeliveryController deliveryController = new DeliveryController();

    @Test
    void testControllerInitialization() {
        // Đảm bảo Controller có thể khởi tạo thành công
        assertNotNull(deliveryController);
    }
}