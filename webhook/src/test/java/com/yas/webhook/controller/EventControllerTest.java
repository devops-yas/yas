package com.yas.webhook.controller;

import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventControllerTest {
    private EventService eventService;
    private EventController eventController;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        eventController = new EventController(eventService);
    }

    @Test
    void listWebhooks_ShouldReturnOk() {
        when(eventService.findAllEvents()).thenReturn(List.of(EventVm.builder().build()));
        ResponseEntity<List<EventVm>> result = eventController.listWebhooks();
        assertEquals(200, result.getStatusCode().value());
    }
}