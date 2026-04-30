package com.yas.sampledata.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SampleDataControllerTest {

    @Test
    void shouldDelegateToService() {
        SampleDataService service = Mockito.mock(SampleDataService.class);
        SampleDataVm expected = new SampleDataVm("ok");
        Mockito.when(service.createSampleData()).thenReturn(expected);

        SampleDataController controller = new SampleDataController(service);
        SampleDataVm actual = controller.createSampleData(new SampleDataVm("ignored"));

        assertEquals(expected, actual);
        Mockito.verify(service).createSampleData();
    }
}
