package com.yas.commonlibrary.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.commonlibrary.kafka.cdc.message.Operation;
import com.yas.commonlibrary.viewmodel.error.ErrorVm;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommonModelTest {

    @Test
    void auditEntityStoresAuditFields() {
        AbstractAuditEntity entity = new AbstractAuditEntity();
        ZonedDateTime createdOn = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        ZonedDateTime lastModifiedOn = ZonedDateTime.parse("2026-01-02T00:00:00Z");

        entity.setCreatedOn(createdOn);
        entity.setCreatedBy("creator");
        entity.setLastModifiedOn(lastModifiedOn);
        entity.setLastModifiedBy("modifier");

        assertEquals(createdOn, entity.getCreatedOn());
        assertEquals("creator", entity.getCreatedBy());
        assertEquals(lastModifiedOn, entity.getLastModifiedOn());
        assertEquals("modifier", entity.getLastModifiedBy());
    }

    @Test
    void operationNamesMatchCdcCodes() {
        assertEquals("r", Operation.READ.getName());
        assertEquals("c", Operation.CREATE.getName());
        assertEquals("u", Operation.UPDATE.getName());
        assertEquals("d", Operation.DELETE.getName());
    }

    @Test
    void errorVmConvenienceConstructorUsesEmptyFieldErrors() {
        ErrorVm error = new ErrorVm("400 BAD_REQUEST", "Bad Request", "Invalid request");

        assertEquals("400 BAD_REQUEST", error.statusCode());
        assertEquals("Bad Request", error.title());
        assertEquals("Invalid request", error.detail());
        assertTrue(error.fieldErrors().isEmpty());
    }

    @Test
    void errorVmStoresFieldErrors() {
        ErrorVm error = new ErrorVm("400 BAD_REQUEST", "Bad Request", "Invalid request", List.of("name required"));

        assertEquals(List.of("name required"), error.fieldErrors());
    }
}
