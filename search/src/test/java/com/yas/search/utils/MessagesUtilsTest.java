package com.yas.search.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void testGetMessage_withValidCode() {
        // Act
        String result = MessagesUtils.getMessage("test.key");

        // Assert
        assertNotNull(result);
        // If resource bundle doesn't have the key, it returns the key itself
        assertEquals("test.key", result);
    }

    @Test
    void testGetMessage_withCodeAndArguments() {
        // Act
        String result = MessagesUtils.getMessage("test.key", "arg1", "arg2");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetMessage_withEmptyCode() {
        // Act
        String result = MessagesUtils.getMessage("");

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }
}
