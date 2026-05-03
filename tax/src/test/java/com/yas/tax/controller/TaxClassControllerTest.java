package com.yas.tax.controller;

import com.yas.tax.model.TaxClass;
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaxClassControllerTest {

    private TaxClassService taxClassService;
    private TaxClassController taxClassController;

    @BeforeEach
    void setUp() {
        taxClassService = mock(TaxClassService.class);
        taxClassController = new TaxClassController(taxClassService);
    }

    @Test
    void getPageableTaxClasses_ShouldReturnOk() {
        when(taxClassService.getPageableTaxClasses(0, 10)).thenReturn(mock(TaxClassListGetVm.class));
        var result = taxClassController.getPageableTaxClasses(0, 10);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listTaxClasses_ShouldReturnOk() {
        when(taxClassService.findAllTaxClasses()).thenReturn(List.of(mock(TaxClassVm.class)));
        var result = taxClassController.listTaxClasses();
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void getTaxClass_ShouldReturnOk() {
        when(taxClassService.findById(1L)).thenReturn(mock(TaxClassVm.class));
        var result = taxClassController.getTaxClass(1L);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void createTaxClass_ShouldReturnCreated() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        when(taxClassService.create(any())).thenReturn(taxClass);

        UriComponentsBuilder builder = mock(UriComponentsBuilder.class);
        UriComponents components = mock(UriComponents.class);
        when(builder.replacePath(anyString())).thenReturn(builder);
        when(builder.buildAndExpand(any(Object[].class))).thenReturn(components);
        when(components.toUri()).thenReturn(URI.create("/tax-classes/1"));

        var result = taxClassController.createTaxClass(mock(TaxClassPostVm.class), builder);
        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    void updateTaxClass_ShouldReturnNoContent() {
        var result = taxClassController.updateTaxClass(1L, mock(TaxClassPostVm.class));
        assertEquals(204, result.getStatusCode().value());
        verify(taxClassService, times(1)).update(any(), eq(1L));
    }

    @Test
    void deleteTaxClass_ShouldReturnNoContent() {
        var result = taxClassController.deleteTaxClass(1L);
        assertEquals(204, result.getStatusCode().value());
        verify(taxClassService, times(1)).delete(1L);
    }
}