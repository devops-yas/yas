package com.yas.tax.service;

import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaxClassServiceTest {

    private TaxClassRepository taxClassRepository;
    private TaxClassService taxClassService;
    private TaxClass taxClass;

    @BeforeEach
    void setUp() {
        taxClassRepository = mock(TaxClassRepository.class);
        taxClassService = new TaxClassService(taxClassRepository);
        
        taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("VAT");
    }

    @Test
    void findAllTaxClasses_ShouldReturnList() {
        when(taxClassRepository.findAll(any(Sort.class))).thenReturn(List.of(taxClass));
        var result = taxClassService.findAllTaxClasses();
        assertEquals(1, result.size());
    }

    @Test
    void findById_ShouldReturnTaxClassVm() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        var result = taxClassService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void create_ShouldReturnTaxClass() {
        // Giả sử TaxClassPostVm có constructor nhận 1 String name (nếu lỗi, bạn báo lại nhé)
        TaxClassPostVm postVm = mock(TaxClassPostVm.class);
        when(postVm.name()).thenReturn("VAT");
        when(postVm.toModel()).thenReturn(taxClass);
        
        when(taxClassRepository.existsByName(anyString())).thenReturn(false);
        when(taxClassRepository.save(any())).thenReturn(taxClass);
        
        var result = taxClassService.create(postVm);
        assertNotNull(result);
    }

    @Test
    void update_ShouldUpdateTaxClass() {
        TaxClassPostVm postVm = mock(TaxClassPostVm.class);
        when(postVm.name()).thenReturn("New VAT");
        
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass(anyString(), anyLong())).thenReturn(false);
        
        taxClassService.update(postVm, 1L);
        verify(taxClassRepository, times(1)).save(taxClass);
    }

    @Test
    void delete_ShouldDeleteTaxClass() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        taxClassService.delete(1L);
        verify(taxClassRepository, times(1)).deleteById(1L);
    }

    @Test
    void getPageableTaxClasses_ShouldReturnPage() {
        Page<TaxClass> page = new PageImpl<>(List.of(taxClass));
        when(taxClassRepository.findAll(any(Pageable.class))).thenReturn(page);
        
        var result = taxClassService.getPageableTaxClasses(0, 10);
        assertNotNull(result);
    }
}