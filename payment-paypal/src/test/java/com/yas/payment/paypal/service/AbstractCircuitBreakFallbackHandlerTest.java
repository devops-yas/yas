package com.yas.payment.paypal.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractCircuitBreakFallbackHandlerTest {

    private final TestFallbackHandler handler = new TestFallbackHandler();

    @Test
    void handleBodilessFallbackThrowsOriginalThrowable() {
        IllegalStateException throwable = new IllegalStateException("payment failed");

        Throwable thrown = assertThrows(Throwable.class, () -> handler.invokeBodilessFallback(throwable));

        assertSame(throwable, thrown);
    }

    @Test
    void handleTypedFallbackThrowsOriginalThrowable() {
        IllegalArgumentException throwable = new IllegalArgumentException("invalid payment");

        Throwable thrown = assertThrows(Throwable.class, () -> handler.invokeTypedFallback(throwable));

        assertSame(throwable, thrown);
    }

    private static class TestFallbackHandler extends AbstractCircuitBreakFallbackHandler {

        private void invokeBodilessFallback(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        private String invokeTypedFallback(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }
}
