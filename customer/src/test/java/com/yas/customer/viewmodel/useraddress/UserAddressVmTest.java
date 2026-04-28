package com.yas.customer.viewmodel.useraddress;

import com.yas.customer.model.UserAddress;
import com.yas.customer.viewmodel.address.AddressVm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAddressVmTest {

    @Test
    void builderShouldCreateUserAddressVm() {
        AddressVm addressVm = AddressVm.builder()
            .id(100L)
            .contactName("Nguyen Van A")
            .phone("0909000000")
            .addressLine1("123 Main Street")
            .city("Ho Chi Minh City")
            .zipCode("700000")
            .districtId(1L)
            .stateOrProvinceId(2L)
            .countryId(3L)
            .build();

        UserAddressVm userAddressVm = UserAddressVm.builder()
            .id(1L)
            .userId("user-001")
            .addressGetVm(addressVm)
            .isActive(true)
            .build();

        assertEquals(1L, userAddressVm.id());
        assertEquals("user-001", userAddressVm.userId());
        assertEquals(addressVm, userAddressVm.addressGetVm());
        assertTrue(userAddressVm.isActive());
    }

    @Test
    void fromModelShouldMapUserAddressAndAddressVm() {
        UserAddress userAddress = UserAddress.builder()
            .id(2L)
            .userId("user-002")
            .addressId(200L)
            .isActive(false)
            .build();

        AddressVm addressVm = AddressVm.builder()
            .id(200L)
            .contactName("Tran Thi B")
            .phone("0911000000")
            .addressLine1("456 Second Street")
            .city("Da Nang")
            .zipCode("550000")
            .districtId(4L)
            .stateOrProvinceId(5L)
            .countryId(6L)
            .build();

        UserAddressVm result = UserAddressVm.fromModel(userAddress, addressVm);

        assertEquals(2L, result.id());
        assertEquals("user-002", result.userId());
        assertEquals(addressVm, result.addressGetVm());
        assertFalse(result.isActive());
    }
}

