package com.nextbar.eventPlanner.dto.request;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nextbar.eventPlanner.model.ResourceMode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateRequestsValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createBarRequest_shouldRequireExistingResourceIdForExistingMode() {
        CreateBarRequest request = CreateBarRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.EXISTING)
                .name("Any Name")
                .build();

        Set<ConstraintViolation<CreateBarRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createBarRequest_shouldRequireNameForNewMode() {
        CreateBarRequest request = CreateBarRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.NEW)
                .existingResourceId("bar-123")
                .build();

        Set<ConstraintViolation<CreateBarRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createBarRequest_existingModeShouldPassWithExistingResourceId() {
        CreateBarRequest request = CreateBarRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.EXISTING)
                .existingResourceId("bar-123")
                .build();

        Set<ConstraintViolation<CreateBarRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void createDropPointRequest_shouldRequireExistingResourceIdForExistingMode() {
        CreateDropPointRequest request = CreateDropPointRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.EXISTING)
                .name("DP-A")
                .capacity(100)
                .build();

        Set<ConstraintViolation<CreateDropPointRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createDropPointRequest_shouldRequireNameAndCapacityForNewMode() {
        CreateDropPointRequest request = CreateDropPointRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.NEW)
                .existingResourceId("dp-123")
                .build();

        Set<ConstraintViolation<CreateDropPointRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createDropPointRequest_existingModeShouldPassWithExistingResourceId() {
        CreateDropPointRequest request = CreateDropPointRequest.builder()
                .eventId(1L)
                .resourceMode(ResourceMode.EXISTING)
                .existingResourceId("dp-123")
                .build();

        Set<ConstraintViolation<CreateDropPointRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
