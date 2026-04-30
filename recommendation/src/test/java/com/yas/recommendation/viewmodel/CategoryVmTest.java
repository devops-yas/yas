package com.yas.recommendation.viewmodel;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CategoryVmTest {

    @Test
    void shouldExposeValuesAndSupportEquality() {
        CategoryVm first = new CategoryVm(
                1L,
                "Books",
                "All books",
                "books",
                "reading",
                "meta description",
                (short) 2,
                true
        );
        CategoryVm second = new CategoryVm(
                1L,
                "Books",
                "All books",
                "books",
                "reading",
                "meta description",
                (short) 2,
                true
        );
        CategoryVm different = new CategoryVm(
                2L,
                "Games",
                null,
                "games",
                null,
                null,
                (short) 1,
                false
        );

        assertAll(
                () -> assertEquals(1L, first.id()),
                () -> assertEquals("Books", first.name()),
                () -> assertEquals("All books", first.description()),
                () -> assertEquals("books", first.slug()),
                () -> assertEquals("reading", first.metaKeyword()),
                () -> assertEquals("meta description", first.metaDescription()),
                () -> assertEquals((short) 2, first.displayOrder()),
                () -> assertEquals(true, first.isPublished())
        );
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertTrue(first.toString().contains("Books"));
    }
}
