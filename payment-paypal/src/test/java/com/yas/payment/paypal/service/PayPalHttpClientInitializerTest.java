package com.yas.payment.paypal.service;

import com.paypal.core.PayPalHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayPalHttpClientInitializerTest {

    private final PayPalHttpClientInitializer initializer = new PayPalHttpClientInitializer();

    @Test
    void createPaypalClientWhenModeIsSandboxReturnsClient() {
        String settings = "{\"clientId\":\"client-id\",\"clientSecret\":\"client-secret\",\"mode\":\"sandbox\"}";

        PayPalHttpClient client = initializer.createPaypalClient(settings);

        assertNotNull(client);
    }

    @Test
    void createPaypalClientWhenModeIsLiveReturnsClient() {
        String settings = "{\"clientId\":\"client-id\",\"clientSecret\":\"client-secret\",\"mode\":\"live\"}";

        PayPalHttpClient client = initializer.createPaypalClient(settings);

        assertNotNull(client);
    }

    @Test
    void createPaypalClientWhenSettingsAreNullThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> initializer.createPaypalClient(null)
        );

        assertNotNull(exception.getMessage());
    }
}
