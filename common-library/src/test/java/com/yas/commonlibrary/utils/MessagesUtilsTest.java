package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.commonlibrary.constants.MessageCode;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessageReturnsFormattedBundleMessage() {
        String message = MessagesUtils.getMessage(MessageCode.PRODUCT_NOT_FOUND, 10);

        assertEquals("The product 10 is not found", message);
    }

    @Test
    void getMessageReturnsCodeWhenMessageIsMissing() {
        String message = MessagesUtils.getMessage("UNKNOWN_CODE", "ignored");

        assertEquals("UNKNOWN_CODE", message);
    }
}
