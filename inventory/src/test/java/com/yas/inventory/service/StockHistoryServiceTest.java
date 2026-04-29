package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {
    @Mock StockHistoryRepository stockHistoryRepository;
    @Mock ProductService productService;

    @InjectMocks StockHistoryService stockHistoryService;

    @Test
    void createStockHistories_Success() {
        Stock stock = Stock.builder().id(1L).productId(1L).build();
        StockQuantityVm vm = new StockQuantityVm(1L, 10L, "Note");

        stockHistoryService.createStockHistories(List.of(stock), List.of(vm));
        verify(stockHistoryRepository).saveAll(any());
    }

    @Test
    void getStockHistories_Success() {
        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(1L, 1L))
            .thenReturn(List.of(new StockHistory()));
        when(productService.getProduct(1L)).thenReturn(new ProductInfoVm(1L, "P", "S", true));

        var result = stockHistoryService.getStockHistories(1L, 1L);
        assertNotNull(result);
    }
}