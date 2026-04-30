package com.yas.payment.paypal.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstantsTest {

    @Test
    void constantsExposeExpectedValues() {
        assertNotNull(new Constants());
        assertEquals("SIGN_IN_REQUIRED", Constants.ErrorCode.SIGN_IN_REQUIRED);
        assertEquals("FORBIDDEN", Constants.ErrorCode.FORBIDDEN);
        assertEquals("PAYMENT_FAIL_MESSAGE", Constants.Message.PAYMENT_FAIL_MESSAGE);
        assertEquals("PAYMENT_SUCCESS_MESSAGE", Constants.Message.PAYMENT_SUCCESS_MESSAGE);
        assertEquals("Yas", Constants.Yas.BRAND_NAME);
    }

    @Test
    void nestedConstantClassesHavePrivateConstructors() throws ReflectiveOperationException {
        assertPrivateConstructor(Constants.ErrorCode.class);
        assertPrivateConstructor(Constants.Message.class);
        assertPrivateConstructor(Constants.Yas.class);
    }

    private void assertPrivateConstructor(Class<?> constantsClass) throws ReflectiveOperationException {
        Constructor<?> constructor = constantsClass.getDeclaredConstructor(Constants.class);
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance(new Constants()));
    }
}
