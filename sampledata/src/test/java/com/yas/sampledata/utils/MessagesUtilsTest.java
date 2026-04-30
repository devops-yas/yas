package com.yas.sampledata.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void shouldReturnCodeWhenMessageMissing() {
        String code = "sampledata.unknown";

        assertEquals(code, MessagesUtils.getMessage(code, "arg"));
    }
}
