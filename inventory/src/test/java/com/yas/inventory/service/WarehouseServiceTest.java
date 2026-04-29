package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.*;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {
    @Mock WarehouseRepository warehouseRepository;
    @Mock LocationService locationService;

    @InjectMocks WarehouseService warehouseService;

    @Test
    void create_ThrowDuplicatedException_WhenNameExists() {
        WarehousePostVm postVm = WarehousePostVm.builder().name("Existed").build();
        when(warehouseRepository.existsByName("Existed")).thenReturn(true);
        assertThrows(DuplicatedException.class, () -> warehouseService.create(postVm));
    }

    @Test
    void findById_Success() {
        Warehouse warehouse = Warehouse.builder().id(1L).addressId(1L).name("Wh").build();
        AddressDetailVm address = new AddressDetailVm(1L, "C", "P", "A1", "A2", "City", "Zip", 1L, "D", 1L, "S", 1L, "C");
        
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(locationService.getAddressById(1L)).thenReturn(address);

        var result = warehouseService.findById(1L);
        assertNotNull(result);
        assertEquals("Wh", result.name());
    }
}