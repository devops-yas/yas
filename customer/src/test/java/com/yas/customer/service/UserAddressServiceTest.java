package com.yas.customer.service;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import com.yas.customer.viewmodel.address.ActiveAddressVm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private UserAddressService userAddressService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
void createAddressShouldCreateInactiveAddressWhenUserAlreadyHasAddress() {
    setCurrentUser("user-001");

    AddressPostVm addressPostVm = new AddressPostVm(
        "Nguyen Van A",
        "0909000000",
        "123 Main Street",
        "Ho Chi Minh City",
        "700000",
        1L,
        2L,
        3L
    );

    AddressVm addressVm = AddressVm.builder()
        .id(300L)
        .contactName("Nguyen Van A")
        .phone("0909000000")
        .addressLine1("123 Main Street")
        .city("Ho Chi Minh City")
        .zipCode("700000")
        .districtId(1L)
        .stateOrProvinceId(2L)
        .countryId(3L)
        .build();

    UserAddress existingAddress = UserAddress.builder()
        .id(1L)
        .userId("user-001")
        .addressId(100L)
        .isActive(true)
        .build();

    UserAddress savedAddress = UserAddress.builder()
        .id(2L)
        .userId("user-001")
        .addressId(300L)
        .isActive(false)
        .build();

    when(userAddressRepository.findAllByUserId("user-001")).thenReturn(List.of(existingAddress));
    when(locationService.createAddress(addressPostVm)).thenReturn(addressVm);
    when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedAddress);

    var result = userAddressService.createAddress(addressPostVm);

    assertEquals(2L, result.id());
    assertEquals("user-001", result.userId());
    assertEquals(addressVm, result.addressGetVm());
    assertFalse(result.isActive());

    verify(userAddressRepository).findAllByUserId("user-001");
    verify(locationService).createAddress(addressPostVm);
    verify(userAddressRepository).save(any(UserAddress.class));
}

@Test
void getUserAddressListShouldReturnActiveAddressesSortedByActiveStatus() {
    setCurrentUser("user-001");

    UserAddress inactiveAddress = UserAddress.builder()
        .id(1L)
        .userId("user-001")
        .addressId(100L)
        .isActive(false)
        .build();

    UserAddress activeAddress = UserAddress.builder()
        .id(2L)
        .userId("user-001")
        .addressId(200L)
        .isActive(true)
        .build();

    AddressDetailVm firstAddressDetail = new AddressDetailVm(
        100L,
        "Inactive User",
        "0909000000",
        "Inactive Street",
        "Ho Chi Minh City",
        "700000",
        1L,
        "District 1",
        2L,
        "Ho Chi Minh",
        3L,
        "Vietnam"
    );

    AddressDetailVm secondAddressDetail = new AddressDetailVm(
        200L,
        "Active User",
        "0911000000",
        "Active Street",
        "Da Nang",
        "550000",
        4L,
        "District 4",
        5L,
        "Da Nang",
        6L,
        "Vietnam"
    );

    when(userAddressRepository.findAllByUserId("user-001"))
        .thenReturn(List.of(inactiveAddress, activeAddress));

    when(locationService.getAddressesByIdList(List.of(100L, 200L)))
        .thenReturn(List.of(firstAddressDetail, secondAddressDetail));

    List<ActiveAddressVm> result = userAddressService.getUserAddressList();

    assertEquals(2, result.size());
    assertTrue(result.get(0).isActive());
    assertFalse(result.get(1).isActive());
    assertEquals(200L, result.get(0).id());
    assertEquals(100L, result.get(1).id());

    verify(userAddressRepository).findAllByUserId("user-001");
    verify(locationService).getAddressesByIdList(List.of(100L, 200L));
    }

    @Test
    void getAddressDefaultShouldThrowAccessDeniedForAnonymousUser() {
        setCurrentUser("anonymousUser");

        assertThrows(AccessDeniedException.class, () -> userAddressService.getAddressDefault());

        verifyNoInteractions(userAddressRepository);
        verifyNoInteractions(locationService);
    }

    @Test
    void getAddressDefaultShouldReturnDefaultAddress() {
        setCurrentUser("user-001");

        UserAddress userAddress = UserAddress.builder()
            .id(1L)
            .userId("user-001")
            .addressId(100L)
            .isActive(true)
            .build();

        AddressDetailVm addressDetailVm = new AddressDetailVm(
            100L,
            "Nguyen Van A",
            "0909000000",
            "123 Main Street",
            "Ho Chi Minh City",
            "700000",
            1L,
            "District 1",
            2L,
            "Ho Chi Minh",
            3L,
            "Vietnam"
        );

        when(userAddressRepository.findByUserIdAndIsActiveTrue("user-001"))
            .thenReturn(Optional.of(userAddress));
        when(locationService.getAddressById(100L)).thenReturn(addressDetailVm);

        AddressDetailVm result = userAddressService.getAddressDefault();

        assertEquals(addressDetailVm, result);
        verify(userAddressRepository).findByUserIdAndIsActiveTrue("user-001");
        verify(locationService).getAddressById(100L);
    }

    @Test
    void getAddressDefaultShouldThrowNotFoundWhenNoDefaultAddress() {
        setCurrentUser("user-001");

        when(userAddressRepository.findByUserIdAndIsActiveTrue("user-001"))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userAddressService.getAddressDefault());

        verify(userAddressRepository).findByUserIdAndIsActiveTrue("user-001");
        verifyNoInteractions(locationService);
    }

    @Test
    void deleteAddressShouldDeleteExistingUserAddress() {
        setCurrentUser("user-001");

        UserAddress userAddress = UserAddress.builder()
            .id(1L)
            .userId("user-001")
            .addressId(100L)
            .isActive(true)
            .build();

        when(userAddressRepository.findOneByUserIdAndAddressId(anyString(), eq(100L)))
            .thenReturn(userAddress);

        userAddressService.deleteAddress(100L);

        verify(userAddressRepository).findOneByUserIdAndAddressId(anyString(), eq(100L));
        verify(userAddressRepository).delete(userAddress);
    }

    @Test
    void deleteAddressShouldThrowNotFoundWhenAddressDoesNotExist() {
        setCurrentUser("user-001");

        when(userAddressRepository.findOneByUserIdAndAddressId(anyString(), eq(100L)))
            .thenReturn(null);

        assertThrows(NotFoundException.class, () -> userAddressService.deleteAddress(100L));

        verify(userAddressRepository).findOneByUserIdAndAddressId(anyString(), eq(100L));
        verify(userAddressRepository, never()).delete(any());
    }

    @Test
    void chooseDefaultAddressShouldActivateOnlySelectedAddress() {
        setCurrentUser("user-001");

        UserAddress firstAddress = UserAddress.builder()
            .id(1L)
            .userId("user-001")
            .addressId(100L)
            .isActive(false)
            .build();

        UserAddress secondAddress = UserAddress.builder()
            .id(2L)
            .userId("user-001")
            .addressId(200L)
            .isActive(true)
            .build();

        List<UserAddress> userAddresses = List.of(firstAddress, secondAddress);

        when(userAddressRepository.findAllByUserId("user-001")).thenReturn(userAddresses);

        userAddressService.chooseDefaultAddress(100L);

        assertTrue(firstAddress.getIsActive());
        assertFalse(secondAddress.getIsActive());
        verify(userAddressRepository).findAllByUserId("user-001");
        verify(userAddressRepository).saveAll(userAddresses);
    }

    private void setCurrentUser(String userId) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}