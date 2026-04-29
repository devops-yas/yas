package com.yas.product.model;

import com.yas.product.model.attribute.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ModelCoverageTest {

    @Test
    void testProductModels() {
        // Quét Product, Category, Brand
        Product product = Product.builder().id(1L).name("P1").build();
        Category category = new Category(); category.setId(1L);

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Brand");
        brand.setSlug("brand");
        assertThat(brand.getName()).isEqualTo("Brand");

        // Quét ProductCategory
        ProductCategory pc = ProductCategory.builder().id(1L).product(product).category(category).build();
        pc.setId(2L);
        assertThat(pc.getId()).isEqualTo(2L);

        // Quét ProductImage
        ProductImage pi = ProductImage.builder().id(1L).imageId(10L).product(product).build();
        pi.setImageId(20L);
        assertThat(pi.getImageId()).isEqualTo(20L);

        // Quét ProductOption
        ProductOption po = new ProductOption();
        po.setId(1L); po.setName("Color");
        assertThat(po.getName()).isEqualTo("Color");

        // Quét ProductOptionValue
        ProductOptionValue pov = ProductOptionValue.builder().id(1L).product(product).productOption(po).value("Red").displayType("text").displayOrder(1).build();
        pov.setValue("Blue");
        assertThat(pov.getValue()).isEqualTo("Blue");
        
        // Quét ProductOptionCombination
        ProductOptionCombination poc = ProductOptionCombination.builder().id(1L).product(product).productOption(po).value("Red").displayOrder(1).build();
        poc.setDisplayOrder(2);
        assertThat(poc.getDisplayOrder()).isEqualTo(2);
        
        // Quét ProductRelated
        ProductRelated pr = ProductRelated.builder().id(1L).product(product).relatedProduct(product).build();
        pr.setId(2L);
        assertThat(pr.getId()).isEqualTo(2L);
    }

    @Test
    void testAttributeModels() {
        // Quét ProductAttributeGroup
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(1L); group.setName("Group");
        assertThat(group.getName()).isEqualTo("Group");

        // Quét ProductAttribute
        ProductAttribute attr = ProductAttribute.builder().id(1L).name("Attr").productAttributeGroup(group).build();
        attr.setName("Attr2");
        assertThat(attr.getName()).isEqualTo("Attr2");

        // Quét ProductAttributeValue (Đã sửa lỗi builder)
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setId(1L);
        attrVal.setProduct(new Product());
        attrVal.setProductAttribute(attr);
        attrVal.setValue("Val");
        
        attrVal.setValue("Val2");
        assertThat(attrVal.getValue()).isEqualTo("Val2");

        // Quét ProductTemplate
        ProductTemplate pt = ProductTemplate.builder().id(1L).name("Template").build();
        pt.setName("Template2");
        assertThat(pt.getName()).isEqualTo("Template2");

        // Quét ProductAttributeTemplate
        ProductAttributeTemplate pat = ProductAttributeTemplate.builder().id(1L).productAttribute(attr).productTemplate(pt).displayOrder(1).build();
        pat.setDisplayOrder(2);
        assertThat(pat.getDisplayOrder()).isEqualTo(2);
    }
}