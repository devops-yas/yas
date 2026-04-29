package com.yas.webhook.controller;

import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import com.yas.webhook.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebhookControllerTest {
    private WebhookService webhookService;
    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        webhookService = mock(WebhookService.class);
        webhookController = new WebhookController(webhookService);
    }

    @Test
    void getPageableWebhooks_ShouldReturnOk() {
        when(webhookService.getPageableWebhooks(0, 10)).thenReturn(mock(WebhookListGetVm.class));
        var result = webhookController.getPageableWebhooks(0, 10);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listWebhooks_ShouldReturnOk() {
        when(webhookService.findAllWebhooks()).thenReturn(List.of(mock(WebhookVm.class)));
        var result = webhookController.listWebhooks();
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void getWebhook_ShouldReturnOk() {
        when(webhookService.findById(1L)).thenReturn(mock(WebhookDetailVm.class));
        var result = webhookController.getWebhook(1L);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void createWebhook_ShouldReturnCreated() {
        WebhookDetailVm detailVm = mock(WebhookDetailVm.class);
        when(webhookService.create(any())).thenReturn(detailVm);

        UriComponentsBuilder builder = mock(UriComponentsBuilder.class);
        UriComponents components = mock(UriComponents.class);
        when(builder.replacePath(anyString())).thenReturn(builder);
        when(builder.buildAndExpand(any(Object.class))).thenReturn(components);
        when(components.toUri()).thenReturn(URI.create("/webhooks/1"));

        var result = webhookController.createWebhook(mock(WebhookPostVm.class), builder);
        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    void updateWebhook_ShouldReturnNoContent() {
        var result = webhookController.updateWebhook(1L, mock(WebhookPostVm.class));
        assertEquals(204, result.getStatusCode().value());
        verify(webhookService).update(any(), eq(1L));
    }

    @Test
    void deleteWebhook_ShouldReturnNoContent() {
        var result = webhookController.deleteWebhook(1L);
        assertEquals(204, result.getStatusCode().value());
        verify(webhookService).delete(1L);
    }
}