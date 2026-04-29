package com.yas.product.controller;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.repository.BrandRepository;
import com.yas.product.service.BrandService;
import com.yas.product.viewmodel.brand.BrandListGetVm;
import com.yas.product.viewmodel.brand.BrandPostVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BrandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        // Vô hiệu hóa Validator để tập trung test logic của Controller
        mockMvc = MockMvcBuilders.standaloneSetup(brandController)
                .setValidator(new org.springframework.validation.Validator() {
                    @Override public boolean supports(Class<?> clazz) { return true; }
                    @Override public void validate(Object target, org.springframework.validation.Errors errors) {}
                })
                .build();
    }

    @Test
    void listBrands_ShouldReturnList() throws Exception {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Samsung");
        
        when(brandRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(brand));

        mockMvc.perform(get("/backoffice/brands")
                        .param("brandName", "Sam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Samsung"));
    }

    @Test
    void getPageableBrands_ShouldReturnPagedResult() throws Exception {
        BrandListGetVm pagedResult = new BrandListGetVm(List.of(), 0, 10, 0, 0, false);
        when(brandService.getBrands(anyInt(), anyInt())).thenReturn(pagedResult);

        mockMvc.perform(get("/backoffice/brands/paging"))
                .andExpect(status().isOk());
    }

    @Test
    void getBrand_ShouldReturnBrand_WhenFound() throws Exception {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Apple");
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        mockMvc.perform(get("/backoffice/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void createBrand_ShouldReturnCreated() throws Exception {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Sony");
        brand.setSlug("sony");
        brand.setPublished(true);
        
        when(brandService.create(any(BrandPostVm.class))).thenReturn(brand);

        // Đã bổ sung trường isPublish vào chuỗi JSON
        String json = "{\"name\": \"Sony\", \"slug\": \"sony\", \"isPublish\": true}";
        
        mockMvc.perform(post("/backoffice/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void updateBrand_ShouldReturnNoContent() throws Exception {
        // Đã bổ sung trường isPublish vào chuỗi JSON
        String json = "{\"name\": \"LG\", \"slug\": \"lg\", \"isPublish\": true}";
        
        mockMvc.perform(put("/backoffice/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
        
        verify(brandService).update(any(BrandPostVm.class), eq(1L));
    }

    @Test
    void deleteBrand_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/backoffice/brands/1"))
                .andExpect(status().isNoContent());
        
        verify(brandService).delete(1L);
    }

    @Test
    void getBrandsByIds_ShouldReturnList() throws Exception {
        when(brandService.getBrandsByIds(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/backoffice/brands/by-ids")
                        .param("ids", "1,2"))
                .andExpect(status().isOk());
    }
}