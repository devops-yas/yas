package com.yas.storefrontbff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CartItemVmTest {

    @Test
    void shouldMapFromCartDetailVm() {
        CartDetailVm detailVm = new CartDetailVm(10L, 99L, 3);

        CartItemVm itemVm = CartItemVm.fromCartDetailVm(detailVm);

        assertEquals(99L, itemVm.productId());
        assertEquals(3, itemVm.quantity());
    }

    @Test
    void shouldSupportValueEquality() {
        CartItemVm vm = new CartItemVm(7L, 2);
        CartItemVm same = new CartItemVm(7L, 2);
        CartItemVm different = new CartItemVm(8L, 2);

        assertEquals(vm, same);
        assertEquals(vm.hashCode(), same.hashCode());
        assertNotEquals(vm, different);
    }
}
