package com.yas.recommendation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ImageVmTest {

    @Test
    void shouldExposeValuesAndSupportEquality() {
        ImageVm first = new ImageVm(10L, "https://cdn.example.com/a.png");
        ImageVm second = new ImageVm(10L, "https://cdn.example.com/a.png");
        ImageVm different = new ImageVm(20L, "https://cdn.example.com/b.png");

        assertEquals(10L, first.id());
        assertEquals("https://cdn.example.com/a.png", first.url());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertTrue(first.toString().contains("a.png"));
    }
}
