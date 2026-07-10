package com.yas.commonlibrary.kafka.cdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;

class BaseCdcConsumerTest {

    private final TestCdcConsumer consumer = new TestCdcConsumer();

    @Test
    void processMessageWithValueDelegatesToConsumer() {
        AtomicReference<String> processedValue = new AtomicReference<>();

        consumer.processValue("product", headers("product-1"), processedValue::set);

        assertEquals("product", processedValue.get());
    }

    @Test
    void processMessageWithKeyAndValueDelegatesToBiConsumer() {
        AtomicReference<String> processedKey = new AtomicReference<>();
        AtomicReference<String> processedValue = new AtomicReference<>();

        consumer.processKeyValue("product-1", "product", headers("product-1"), (key, value) -> {
            processedKey.set(key);
            processedValue.set(value);
        });

        assertEquals("product-1", processedKey.get());
        assertEquals("product", processedValue.get());
    }

    private MessageHeaders headers(String key) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(KafkaHeaders.RECEIVED_KEY, key);
        return new MessageHeaders(headers);
    }

    private static class TestCdcConsumer extends BaseCdcConsumer<String, String> {

        void processValue(String record, MessageHeaders headers, java.util.function.Consumer<String> consumer) {
            processMessage(record, headers, consumer);
        }

        void processKeyValue(String key, String value, MessageHeaders headers,
                             java.util.function.BiConsumer<String, String> consumer) {
            processMessage(key, value, headers, consumer);
        }
    }
}
