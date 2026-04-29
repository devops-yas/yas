package com.yas.tax.controller;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaxRateControllerTest {

    private TaxRateService taxRateService;
    private TaxRateController taxRateController;

    @BeforeEach
    void setUp() {
        taxRateService = mock(TaxRateService.class);
        taxRateController = new TaxRateController(taxRateService);
    }

    @Test
    void getPageableTaxRates_ShouldReturnOk() {
        when(taxRateService.getPageableTaxRates(0, 10)).thenReturn(mock(TaxRateListGetVm.class));
        var result = taxRateController.getPageableTaxRates(0, 10);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void getTaxRate_ShouldReturnOk() {
        when(taxRateService.findById(1L)).thenReturn(mock(TaxRateVm.class));
        var result = taxRateController.getTaxRate(1L);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void createTaxRate_ShouldReturnCreated() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setTaxClass(taxClass);

        when(taxRateService.createTaxRate(any())).thenReturn(taxRate);

        UriComponentsBuilder builder = mock(UriComponentsBuilder.class);
        UriComponents components = mock(UriComponents.class);
        when(builder.replacePath(anyString())).thenReturn(builder);
        when(builder.buildAndExpand(any(Object[].class))).thenReturn(components);
        when(components.toUri()).thenReturn(URI.create("/tax-rates/1"));

        var result = taxRateController.createTaxRate(mock(TaxRatePostVm.class), builder);
        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    void updateTaxRate_ShouldReturnNoContent() {
        var result = taxRateController.updateTaxRate(1L, mock(TaxRatePostVm.class));
        assertEquals(204, result.getStatusCode().value());
        verify(taxRateService, times(1)).updateTaxRate(any(), eq(1L));
    }

    @Test
    void deleteTaxRate_ShouldReturnNoContent() {
        var result = taxRateController.deleteTaxRate(1L);
        assertEquals(204, result.getStatusCode().value());
        verify(taxRateService, times(1)).delete(1L);
    }

    @Test
    void getTaxPercentByAddress_ShouldReturnOk() {
        when(taxRateService.getTaxPercent(1L, 1L, 1L, "70000")).thenReturn(10.0);
        var result = taxRateController.getTaxPercentByAddress(1L, 1L, 1L, "70000");
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void getBatchTaxPercentsByAddress_ShouldReturnOk() {
        when(taxRateService.getBulkTaxRate(anyList(), anyLong(), anyLong(), anyString()))
                .thenReturn(List.of(mock(TaxRateVm.class)));
        var result = taxRateController.getBatchTaxPercentsByAddress(List.of(1L), 1L, 1L, "70000");
        assertEquals(200, result.getStatusCode().value());
    }
}