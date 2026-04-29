package com.yas.webhook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.webhook.integration.api.WebhookApi;
import com.yas.webhook.model.Event;
import com.yas.webhook.model.Webhook;
import com.yas.webhook.model.WebhookEvent;
import com.yas.webhook.model.WebhookEventNotification;
import com.yas.webhook.model.dto.WebhookEventNotificationDto;
import com.yas.webhook.model.mapper.WebhookMapper;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import com.yas.webhook.repository.EventRepository;
import com.yas.webhook.repository.WebhookEventNotificationRepository;
import com.yas.webhook.repository.WebhookEventRepository;
import com.yas.webhook.repository.WebhookRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock WebhookRepository webhookRepository;
    @Mock EventRepository eventRepository;
    @Mock WebhookEventRepository webhookEventRepository;
    @Mock WebhookEventNotificationRepository webhookEventNotificationRepository;
    @Mock WebhookMapper webhookMapper;
    @Mock WebhookApi webHookApi;

    @InjectMocks
    WebhookService webhookService;

    // Test cũ của bạn đã được giữ lại
    @Test
    void test_notifyToWebhook_ShouldNotException() {
        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
            .builder()
            .notificationId(1L)
            .url("")
            .secret("")
            .build();

        WebhookEventNotification notification = new WebhookEventNotification();
        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
            .thenReturn(Optional.of(notification));

        webhookService.notifyToWebhook(notificationDto);

        verify(webhookEventNotificationRepository).save(notification);
        verify(webHookApi).notify(notificationDto.getUrl(), notificationDto.getSecret(), notificationDto.getPayload());
    }

    // Các test mới bổ sung
    @Test
    void getPageableWebhooks_ShouldReturnPage() {
        Page<Webhook> page = new PageImpl<>(List.of(new Webhook()));
        when(webhookRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(webhookMapper.toWebhookListGetVm(any(), anyInt(), anyInt())).thenReturn(mock(WebhookListGetVm.class));

        var result = webhookService.getPageableWebhooks(0, 10);
        assertNotNull(result);
    }

    @Test
    void findAllWebhooks_ShouldReturnList() {
        when(webhookRepository.findAll(any(Sort.class))).thenReturn(List.of(new Webhook()));
        when(webhookMapper.toWebhookVm(any())).thenReturn(mock(WebhookVm.class));

        var result = webhookService.findAllWebhooks();
        assertEquals(1, result.size());
    }

    @Test
    void findById_ShouldReturnWebhookDetail() {
        when(webhookRepository.findById(1L)).thenReturn(Optional.of(new Webhook()));
        when(webhookMapper.toWebhookDetailVm(any())).thenReturn(mock(WebhookDetailVm.class));

        var result = webhookService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void create_ShouldSaveAndReturnWebhook_WithoutEvents() {
        WebhookPostVm postVm = mock(WebhookPostVm.class);
        Webhook webhook = new Webhook();
        webhook.setId(1L);

        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(webhook);
        when(webhookRepository.save(webhook)).thenReturn(webhook);
        when(postVm.getEvents()).thenReturn(null);
        when(webhookMapper.toWebhookDetailVm(webhook)).thenReturn(mock(WebhookDetailVm.class));

        var result = webhookService.create(postVm);
        assertNotNull(result);
        verify(webhookEventRepository, never()).saveAll(any());
    }

    @Test
    void create_ShouldSaveAndReturnWebhook_WithEvents() {
        WebhookPostVm postVm = mock(WebhookPostVm.class);
        EventVm eventVm = mock(EventVm.class);
        when(eventVm.getId()).thenReturn(1L);

        Webhook webhook = new Webhook();
        webhook.setId(1L);

        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(webhook);
        when(webhookRepository.save(webhook)).thenReturn(webhook);
        when(postVm.getEvents()).thenReturn(List.of(eventVm));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(new Event()));
        when(webhookEventRepository.saveAll(any())).thenReturn(List.of(new WebhookEvent()));
        when(webhookMapper.toWebhookDetailVm(webhook)).thenReturn(mock(WebhookDetailVm.class));

        var result = webhookService.create(postVm);
        assertNotNull(result);
        verify(webhookEventRepository).saveAll(any());
    }

    @Test
    void update_ShouldUpdateWebhook_WithEvents() {
        WebhookPostVm postVm = mock(WebhookPostVm.class);
        EventVm eventVm = mock(EventVm.class);
        when(eventVm.getId()).thenReturn(1L);

        Webhook webhook = new Webhook();
        webhook.setId(1L);
        WebhookEvent oldEvent = new WebhookEvent();
        webhook.setWebhookEvents(List.of(oldEvent));

        when(webhookRepository.findById(1L)).thenReturn(Optional.of(webhook));
        when(webhookMapper.toUpdatedWebhook(webhook, postVm)).thenReturn(webhook);
        when(postVm.getEvents()).thenReturn(List.of(eventVm));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(new Event()));

        webhookService.update(postVm, 1L);

        verify(webhookRepository).save(webhook);
        verify(webhookEventRepository).deleteAll(anyList());
        verify(webhookEventRepository).saveAll(anyList());
    }

    @Test
    void delete_ShouldDeleteWebhook() {
        when(webhookRepository.existsById(1L)).thenReturn(true);
        webhookService.delete(1L);
        verify(webhookEventRepository).deleteByWebhookId(1L);
        verify(webhookRepository).deleteById(1L);
    }
}