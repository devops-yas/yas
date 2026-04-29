package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.repository.StockHistoryRepository; // Thêm import này
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StockServiceTest {
    @Mock StockRepository stockRepository;
    @Mock WarehouseRepository warehouseRepository;
    @Mock StockHistoryRepository stockHistoryRepository; // Đã thêm Mock bị thiếu
    @Mock ProductService productService;
    @Mock WarehouseService warehouseService;
    @Mock StockHistoryService stockHistoryService;

    @InjectMocks StockService stockService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void addProductIntoWarehouse_Success() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(new ProductInfoVm(1L, "P", "S", true));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(new Warehouse()));

        assertDoesNotThrow(() -> stockService.addProductIntoWarehouse(List.of(postVm)));
        verify(stockRepository).saveAll(any());
    }

    @Test
    void addProductIntoWarehouse_ThrowStockExistingException() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(true);
        assertThrows(StockExistingException.class, () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void updateProductQuantityInStock_Success() {
        Stock stock = Stock.builder().id(1L).quantity(100L).productId(1L).build();
        StockQuantityVm item = new StockQuantityVm(1L, 50L, "Note");
        StockQuantityUpdateVm request = new StockQuantityUpdateVm(List.of(item));

        when(stockRepository.findAllById(any())).thenReturn(List.of(stock));
        
        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(request));
        assertEquals(150L, stock.getQuantity());
        verify(stockRepository).saveAll(any());
    }

    @Test
    void updateProductQuantityInStock_HandleNegativeQuantity() {
        // Sửa lại test này để pass và lấy coverage cho nhánh if (adjustedQuantity < 0)
        Stock stock = Stock.builder().id(1L).quantity(100L).build();
        StockQuantityVm item = new StockQuantityVm(1L, -50L, "Note"); 
        StockQuantityUpdateVm request = new StockQuantityUpdateVm(List.of(item));

        when(stockRepository.findAllById(any())).thenReturn(List.of(stock));
        
        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(request));
        assertEquals(50L, stock.getQuantity());
    }

    @Test
    void updateProductQuantityInStock_SkipWhenVmNull() {
        Stock stock = Stock.builder().id(1L).quantity(100L).build();
        // Item có ID không khớp để giả lập trường hợp không tìm thấy VM match với Stock
        StockQuantityVm item = new StockQuantityVm(99L, 50L, "Note");
        StockQuantityUpdateVm request = new StockQuantityUpdateVm(List.of(item));

        when(stockRepository.findAllById(any())).thenReturn(List.of(stock));
        
        assertDoesNotThrow(() -> stockService.updateProductQuantityInStock(request));
        assertEquals(100L, stock.getQuantity()); // Không thay đổi
    }
}