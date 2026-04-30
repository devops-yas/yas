package com.yas.sampledata.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SampleDataVmTest {

    @Test
    void shouldExposeMessage() {
        SampleDataVm vm = new SampleDataVm("Insert Sample Data successfully!");

        assertEquals("Insert Sample Data successfully!", vm.message());
    }
}
