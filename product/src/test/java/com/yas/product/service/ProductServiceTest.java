package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductOptionValueRepository productOptionValueRepository;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock private ProductRelatedRepository productRelatedRepository;

    @InjectMocks private ProductService productService;

    @Test
    void createProduct_HappyPath_ShouldReturnProductGetDetailVm() {
        // 1. ARRANGE - Chuẩn bị data siêu cơ bản để qua mặt Validation
        ProductPostVm postVm = new ProductPostVm(
            "Laptop", "laptop", 1L, List.of(1L), "Short", "Desc", "Spec", "SKU123", "GTIN123",
            10.0, DimensionUnit.CM, 20.0, 10.0, 10.0, 1000.0, true, true, true, true, true,
            "Meta", "Keyword", "MetaDesc", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L
        );
        
        Brand brand = new Brand(); 
        brand.setId(1L);
        
        Category category = new Category(); 
        category.setId(1L);
        
        Product savedProduct = new Product(); 
        savedProduct.setId(100L); 
        savedProduct.setName("Laptop");

        // Giả lập các Repository trả về data thành công
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // 2. ACT
        ProductGetDetailVm result = productService.createProduct(postVm);

        // 3. ASSERT - Quét qua hàng chục dòng lệnh gán dữ liệu
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getProductById_HappyPath_ShouldReturnProductDetailVm() {
        // 1. ARRANGE
        Product product = new Product();
        product.setId(1L);
        product.setName("Iphone 15");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // 2. ACT
        ProductDetailVm result = productService.getProductById(1L);

        // 3. ASSERT
        assertNotNull(result);
        assertEquals("Iphone 15", result.name());
        assertEquals(1L, result.id());
    }

    @Test
    void getProductDetail_HappyPath_ShouldReturnProductDetailGetVm() {
        // 1. ARRANGE
        Product product = new Product();
        product.setId(1L);
        product.setName("Samsung S24");
        product.setThumbnailMediaId(10L);

        when(productRepository.findBySlugAndIsPublishedTrue("samsung-s24"))
            .thenReturn(Optional.of(product));
            
        NoFileMediaVm mediaVm = new NoFileMediaVm(10L, "caption", "file.jpg", "image/jpeg", "http://image.url");
        when(mediaService.getMedia(10L)).thenReturn(mediaVm);

        // 2. ACT
        ProductDetailGetVm result = productService.getProductDetail("samsung-s24");

        // 3. ASSERT
        assertNotNull(result);
        assertEquals("Samsung S24", result.name());
        assertEquals("http://image.url", result.thumbnailMediaUrl());
    }
    @Test
    void deleteProduct_HappyPath_ShouldDeactivateProduct() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        productService.deleteProduct(1L);
        
        // Kiểm tra xem đã gọi lệnh save để vô hiệu hóa (isPublished = false) chưa
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void exportProducts_HappyPath_ShouldReturnList() {
        Product product = new Product();
        product.setId(10L);
        product.setName("Export Product");
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        product.setBrand(brand);
        
        when(productRepository.getExportingProducts(anyString(), anyString())).thenReturn(List.of(product));
        
        var result = productService.exportProducts("test", "test");
        
        assertEquals(1, result.size());
        assertEquals("Export Product", result.get(0).name());
    }

    @Test
    void updateProductQuantity_HappyPath_ShouldUpdateStock() {
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(5L);
        
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));
        
        com.yas.product.viewmodel.product.ProductQuantityPostVm vm = 
            new com.yas.product.viewmodel.product.ProductQuantityPostVm(1L, 15L);
        productService.updateProductQuantity(List.of(vm));
        
        verify(productRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getProductsWithFilter_HappyPath_ShouldReturnPage() {
        Product product = new Product();
        product.setId(1L);
        org.springframework.data.domain.Page<Product> page = new org.springframework.data.domain.PageImpl<>(List.of(product));
        
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any())).thenReturn(page);
        
        var result = productService.getProductsWithFilter(0, 10, "test", "test");
        
        assertEquals(1, result.productContent().size());
    }

    @Test
    void getLatestProducts_HappyPath_ShouldReturnList() {
        Product product = new Product();
        product.setId(1L);
        
        when(productRepository.getLatestProducts(any())).thenReturn(List.of(product));
        
        var result = productService.getLatestProducts(5);
        
        assertEquals(1, result.size());
    }
    @Test
    void updateProduct_HappyPath_ShouldUpdateSuccessfully() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setProductCategories(new java.util.ArrayList<>());
        existingProduct.setRelatedProducts(new java.util.ArrayList<>());
        existingProduct.setProductImages(new java.util.ArrayList<>());
        existingProduct.setProducts(new java.util.ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        
        ProductPutVm putVm = new ProductPutVm(
                "Laptop Pro", "laptop-pro", 1500.0,
                true, true, true, true, true,
                1L, List.of(2L), "Short",
                "Desc", "Spec", "SKU123", "GTIN123",
                10.0, DimensionUnit.CM, 10.0, 10.0, 10.0, "Meta", "Key",
                "MetaDesc", 1L, List.of(), List.of(), List.of(),
                List.of(), List.of(), 1L
        );

        Brand brand = new Brand();
        brand.setId(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        Category category = new Category();
        category.setId(2L);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        // [MỚI BỔ SUNG] Mock để vượt qua lỗi không tìm thấy Option
        com.yas.product.model.ProductOption mockOption = new com.yas.product.model.ProductOption();
        mockOption.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(mockOption));

        productService.updateProduct(1L, putVm);

        verify(productRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getProductEsDetailById_HappyPath_ShouldReturnDetail() {
        Product product = new Product();
        product.setId(1L);
        product.setName("ES Product");
        product.setProductCategories(List.of());
        product.setAttributeValues(List.of());
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        var result = productService.getProductEsDetailById(1L);
        
        assertEquals("ES Product", result.name());
    }

    @Test
    void getProductCheckoutList_HappyPath_ShouldReturnList() {
        Product product = new Product();
        product.setId(1L);
        product.setThumbnailMediaId(10L); 
        
        // [MỚI BỔ SUNG] Gắn Brand vào Product để tránh lỗi NullPointer khi mapping
        Brand brand = new Brand();
        brand.setId(1L);
        product.setBrand(brand);
        
        org.springframework.data.domain.Page<Product> page = new org.springframework.data.domain.PageImpl<>(List.of(product));
        
        when(productRepository.findAllPublishedProductsByIds(anyList(), any())).thenReturn(page);
        
        com.yas.product.viewmodel.NoFileMediaVm mediaVm = new com.yas.product.viewmodel.NoFileMediaVm(10L, "cap", "file.jpg", "image/jpeg", "http://url");
        when(mediaService.getMedia(any())).thenReturn(mediaVm);
        
        var result = productService.getProductCheckoutList(0, 10, List.of(1L));
        
        assertNotNull(result);
    }

    @Test
    void getProductsFromCategory_HappyPath_ShouldReturnPage() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.of(category));
        
        Product product = new Product();
        product.setId(1L);
        product.setThumbnailMediaId(10L); // Gán ID ảnh để tránh lỗi null
        com.yas.product.model.ProductCategory pc = new com.yas.product.model.ProductCategory();
        pc.setProduct(product);
        
        org.springframework.data.domain.Page<com.yas.product.model.ProductCategory> page = new org.springframework.data.domain.PageImpl<>(List.of(pc));
        when(productCategoryRepository.findAllByCategory(any(), any())).thenReturn(page);

        // Bổ sung mock cho mediaService để trả về URL hợp lệ
        NoFileMediaVm mediaVm = new NoFileMediaVm(10L, "cap", "file.jpg", "image/jpeg", "http://url");
        when(mediaService.getMedia(any())).thenReturn(mediaVm);
        
        var result = productService.getProductsFromCategory(0, 10, "test-cat");
        
        assertEquals(1, result.productContent().size());
    }
    @Test
    void validateProductVm_ShouldThrowDuplicatedException_WhenSlugExists() {
        ProductPostVm postVm = new ProductPostVm(
            "Name", "slug-exists", 1L, List.of(2L), "Short",
            "Desc", "Spec", "SKU123", "GTIN123",
            10.0, DimensionUnit.CM, 10.0, 10.0, 10.0, 100.0,
            true, true, true, true, true,
            "Meta", "Key", "MetaDesc", 1L,
            null, null, null, null, null, 1L
        );
        
        // Cố tình tạo một Product có ID khác với ID đang thao tác để hệ thống báo trùng
        Product mockProduct = new Product();
        mockProduct.setId(99L); 
        
        // Ép Repository trả về Product này khi check Slug
        when(productRepository.findBySlugAndIsPublishedTrue("slug-exists")).thenReturn(Optional.of(mockProduct));
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.yas.commonlibrary.exception.DuplicatedException.class, 
            () -> productService.createProduct(postVm)
        );
    }

    @Test
    void validateProductVm_ShouldThrowDuplicatedException_WhenSkuExists() {
        ProductPutVm putVm = new ProductPutVm(
            "Laptop Pro", "laptop-pro", 1500.0,
            true, true, true, true, true,
            1L, List.of(2L), "Short",
            "Desc", "Spec", "SKU-EXISTS", "GTIN123",
            10.0, DimensionUnit.CM, 10.0, 10.0, 10.0,
            "Meta", "Key", "MetaDesc", 1L,
            null, null, null, null, null, 1L
        );
        
        Product mockProduct = new Product();
        mockProduct.setId(99L); 
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(productRepository.findBySlugAndIsPublishedTrue("laptop-pro")).thenReturn(Optional.empty()); // Cho qua slug
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-EXISTS")).thenReturn(Optional.of(mockProduct)); // Đánh gục ở SKU
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.yas.commonlibrary.exception.DuplicatedException.class, 
            () -> productService.updateProduct(1L, putVm)
        );
    }

    @Test
    void getProductById_ShouldThrowNotFoundException_WhenIdDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.yas.commonlibrary.exception.NotFoundException.class, 
            () -> productService.getProductById(999L)
        );
    }
}