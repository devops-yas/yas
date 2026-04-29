package com.yas.webhook.model.mapper;

import com.yas.webhook.model.Event;
import com.yas.webhook.model.Webhook;
import com.yas.webhook.model.WebhookEvent;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapperCoverageTest {

    // Khởi tạo trực tiếp class do MapStruct tự động sinh ra lúc compile
    private final EventMapper eventMapper = new EventMapperImpl();
    private final WebhookMapper webhookMapper = new WebhookMapperImpl();

    @Test
    void testEventMapper() {
        // Quét nhánh if (event == null)
        assertNull(eventMapper.toEventVm(null));

        // Quét nhánh mapping thông thường
        Event event = new Event();
        event.setId(1L);
        assertNotNull(eventMapper.toEventVm(event));
    }

    @Test
    void testWebhookMapper_NullSafety() {
        // Quét toàn bộ các hàm if (object == null) ẩn của WebhookMapper
        assertNull(webhookMapper.toWebhookVm(null));
        assertNull(webhookMapper.toCreatedWebhook(null));
        assertNull(webhookMapper.toWebhookDetailVm(null));

        Webhook target = new Webhook();
        assertNotNull(webhookMapper.toUpdatedWebhook(target, null));
    }

    @Test
    void testWebhookMapper_toWebhookEventVms() {
        // Quét 2 nhánh: CollectionUtils.isEmpty() là true
        assertTrue(webhookMapper.toWebhookEventVms(null).isEmpty());
        assertTrue(webhookMapper.toWebhookEventVms(new ArrayList<>()).isEmpty());

        // Quét nhánh CollectionUtils.isEmpty() là false
        WebhookEvent event = new WebhookEvent();
        event.setEventId(99L);
        List<EventVm> vms = webhookMapper.toWebhookEventVms(List.of(event));
        
        assertEquals(1, vms.size());
    }

    @Test
    void testWebhookMapper_toWebhookListGetVm() {
        // Quét default method toWebhookListGetVm
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        Page<Webhook> page = new PageImpl<>(List.of(webhook));

        var result = webhookMapper.toWebhookListGetVm(page, 0, 10);
        assertNotNull(result);
    }

    @Test
    void testWebhookMapper_ObjectMappings() {
        // Quét toWebhookVm
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        assertNotNull(webhookMapper.toWebhookVm(webhook));

        // Quét toCreatedWebhook bằng mock để tránh lỗi sai tên constructor
        WebhookPostVm postVm = mock(WebhookPostVm.class);
        assertNotNull(webhookMapper.toCreatedWebhook(postVm));

        // Quét toUpdatedWebhook
        Webhook target = new Webhook();
        assertNotNull(webhookMapper.toUpdatedWebhook(target, postVm));

        // Quét toWebhookDetailVm
        Webhook created = new Webhook();
        WebhookEvent hookEvent = new WebhookEvent();
        hookEvent.setEventId(1L);
        created.setWebhookEvents(List.of(hookEvent));
        
        assertNotNull(webhookMapper.toWebhookDetailVm(created));
    }
}