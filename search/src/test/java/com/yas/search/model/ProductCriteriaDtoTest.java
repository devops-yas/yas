package com.yas.search.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.search.constant.enums.SortType;
import org.junit.jupiter.api.Test;

class ProductCriteriaDtoTest {

    @Test
    void testProductCriteriaDto_withAllData() {
        // Arrange
        String keyword = "laptop";
        Integer page = 0;
        Integer size = 10;
        String brand = "Dell";
        String category = "Electronics";
        String attribute = "RAM-16GB";
        Double minPrice = 500.0;
        Double maxPrice = 1500.0;
        SortType sortType = SortType.PRICE_ASC;

        // Act
        ProductCriteriaDto dto = new ProductCriteriaDto(
            keyword, page, size, brand, category, attribute,
            minPrice, maxPrice, sortType
        );

        // Assert
        assertEquals(keyword, dto.keyword());
        assertEquals(page, dto.page());
        assertEquals(size, dto.size());
        assertEquals(brand, dto.brand());
        assertEquals(category, dto.category());
        assertEquals(attribute, dto.attribute());
        assertEquals(minPrice, dto.minPrice());
        assertEquals(maxPrice, dto.maxPrice());
        assertEquals(sortType, dto.sortType());
    }

    @Test
    void testProductCriteriaDto_withNullValues() {
        // Arrange & Act
        ProductCriteriaDto dto = new ProductCriteriaDto(
            null, 1, 15, null, null, null, 100.0, 2000.0, null
        );

        // Assert
        assertNull(dto.keyword());
        assertEquals(1, dto.page());
        assertEquals(15, dto.size());
        assertNull(dto.brand());
        assertNull(dto.category());
        assertNull(dto.attribute());
        assertEquals(100.0, dto.minPrice());
        assertEquals(2000.0, dto.maxPrice());
        assertNull(dto.sortType());
    }
}
