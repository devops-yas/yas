package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.lenient;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = TaxRateService.class)
public class TaxServiceTest {
    @MockitoBean
    TaxRateRepository taxRateRepository;
    @MockitoBean
    LocationService locationService;
    @MockitoBean
    TaxClassRepository taxClassRepository;

    @Autowired
    TaxRateService taxRateService;

    TaxRate taxRate;
    @BeforeEach
    void setUp() {
        TaxClass taxClass = Instancio.create(TaxClass.class);
        taxRate = Instancio.of(TaxRate.class)
            .set(field("taxClass"), taxClass)
            .create();
        lenient().when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));
    }

    @Test
    void  testFindAll_shouldReturnAllTaxRate() {
        // run
        List<TaxRateVm> result = taxRateService.findAll();
        // assert
        assertThat(result).hasSize(1).contains(TaxRateVm.fromModel(taxRate));
    }
    @Test
    void findById_HappyPath_ShouldReturnTaxRateVm() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        
        // Lưu ý: Nếu hàm lấy chi tiết trong service của bạn tên là getTaxRate thì hãy sửa lại nhé
        var result = taxRateService.findById(1L); 
        
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void create_HappyPath_ShouldSaveAndReturnTaxRate() {
        com.yas.tax.viewmodel.taxrate.TaxRatePostVm postVm = new com.yas.tax.viewmodel.taxrate.TaxRatePostVm(
                10.0, "70000", 1L, 1L, 1L
        );

        // Đã sửa lại mock cho đúng với logic của service
        when(taxClassRepository.existsById(anyLong())).thenReturn(true);
        when(taxClassRepository.getReferenceById(anyLong())).thenReturn(new com.yas.tax.model.TaxClass());
        when(taxRateRepository.save(any())).thenReturn(taxRate);
        
        var result = taxRateService.createTaxRate(postVm); 
        
        verify(taxRateRepository, times(1)).save(any());
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }

    @Test
    void update_HappyPath_ShouldUpdateSuccessfully() {
        com.yas.tax.viewmodel.taxrate.TaxRatePostVm putVm = new com.yas.tax.viewmodel.taxrate.TaxRatePostVm(
                15.0, "80000", 1L, 1L, 1L
        );

        when(taxRateRepository.findById(1L)).thenReturn(java.util.Optional.of(taxRate));
        
        // Đã sửa lại mock cho đúng với logic của service
        when(taxClassRepository.existsById(anyLong())).thenReturn(true);
        when(taxClassRepository.getReferenceById(anyLong())).thenReturn(new com.yas.tax.model.TaxClass());
        
        taxRateService.updateTaxRate(putVm, 1L);
        
        verify(taxRateRepository, times(1)).save(taxRate);
    }

    @Test
    void delete_HappyPath_ShouldDeleteSuccessfully() {
        // Hàm delete dùng existsById và deleteById thay vì findById
        when(taxRateRepository.existsById(1L)).thenReturn(true);
        
        taxRateService.delete(1L);
        
        verify(taxRateRepository, times(1)).deleteById(1L);
    }
    @Test
    void getPageableTaxRates_ShouldReturnPageableList() {
        // Giả lập dữ liệu phân trang
        com.yas.tax.model.TaxClass mockTaxClass = new com.yas.tax.model.TaxClass();
        mockTaxClass.setName("VAT");
        
        com.yas.tax.model.TaxRate mockRate = new com.yas.tax.model.TaxRate();
        mockRate.setId(1L);
        mockRate.setRate(10.0);
        mockRate.setZipCode("70000");
        mockRate.setTaxClass(mockTaxClass);
        mockRate.setStateOrProvinceId(1L);
        mockRate.setCountryId(1L);

        org.springframework.data.domain.Page<com.yas.tax.model.TaxRate> page = 
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(mockRate));
            
        when(taxRateRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        // Đã gọt lại đúng 3 tham số: Long, String, String
        com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm locVm = 
            new com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm(1L, "HCM", "VN");
            
        when(locationService.getStateOrProvinceAndCountryNames(anyList()))
            .thenReturn(java.util.List.of(locVm));

        var result = taxRateService.getPageableTaxRates(0, 10);

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(1, result.taxRateGetDetailContent().size());
    }

    @Test
    void getTaxPercent_ShouldReturnPercent_WhenNotNull() {
        when(taxRateRepository.getTaxPercent(any(), any(), any(), any())).thenReturn(10.0);
        
        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "70000");
        
        org.junit.jupiter.api.Assertions.assertEquals(10.0, result);
    }

    @Test
    void getTaxPercent_ShouldReturnZero_WhenNull() {
        when(taxRateRepository.getTaxPercent(any(), any(), any(), any())).thenReturn(null);
        
        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "70000");
        
        org.junit.jupiter.api.Assertions.assertEquals(0.0, result);
    }

    @Test
    void getBulkTaxRate_ShouldReturnList() {
        // Khởi tạo TaxClass giả
        com.yas.tax.model.TaxClass mockTaxClass = new com.yas.tax.model.TaxClass();
        mockTaxClass.setId(1L);
        mockTaxClass.setName("VAT");

        // Khởi tạo TaxRate giả và nhét TaxClass vào
        com.yas.tax.model.TaxRate mockRate = new com.yas.tax.model.TaxRate();
        mockRate.setId(1L);
        mockRate.setRate(10.0);
        mockRate.setTaxClass(mockTaxClass);

        when(taxRateRepository.getBatchTaxRates(any(), any(), any(), any()))
            .thenReturn(java.util.List.of(mockRate));
            
        var result = taxRateService.getBulkTaxRate(java.util.List.of(1L), 1L, 1L, "70000");
        
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
    }
}
