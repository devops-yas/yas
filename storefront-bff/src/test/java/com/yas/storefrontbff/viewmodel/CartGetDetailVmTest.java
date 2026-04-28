package com.yas.storefrontbff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CartGetDetailVmTest {

    @Test
    void shouldExposeCartInformationAndDetails() {
        CartDetailVm first = new CartDetailVm(1L, 101L, 2);
        CartDetailVm second = new CartDetailVm(2L, 102L, 4);
        CartGetDetailVm vm = new CartGetDetailVm(9L, "customer-1", List.of(first, second));

        assertEquals(9L, vm.id());
        assertEquals("customer-1", vm.customerId());
        assertEquals(2, vm.cartDetails().size());
        assertEquals(101L, vm.cartDetails().get(0).productId());
    }

    @Test
    void shouldSupportValueEquality() {
        List<CartDetailVm> details = List.of(new CartDetailVm(1L, 101L, 2));
        CartGetDetailVm vm = new CartGetDetailVm(9L, "customer-1", details);
        CartGetDetailVm same = new CartGetDetailVm(9L, "customer-1", details);
        CartGetDetailVm different = new CartGetDetailVm(10L, "customer-1", details);

        assertEquals(vm, same);
        assertEquals(vm.hashCode(), same.hashCode());
        assertNotEquals(vm, different);
    }
}
