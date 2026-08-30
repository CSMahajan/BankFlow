package com.bankflow.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimitServiceTest {

    @Test
    void isAllowed_shouldAllowRequestWhenLimitIsNotReached() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        boolean result = service.isAllowed(
                "user-1",
                3,
                Duration.ofMinutes(1)
        );

        assertTrue(result);
    }

    @Test
    void isAllowed_shouldAllowRequestsUpToLimit() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMinutes(1);

        assertTrue(service.isAllowed("user-1", 3, window));
        assertTrue(service.isAllowed("user-1", 3, window));
        assertTrue(service.isAllowed("user-1", 3, window));
    }

    @Test
    void isAllowed_shouldRejectRequestWhenLimitIsExceeded() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMinutes(1);

        assertTrue(service.isAllowed("user-1", 2, window));
        assertTrue(service.isAllowed("user-1", 2, window));

        boolean result =
                service.isAllowed("user-1", 2, window);

        assertFalse(result);
    }

    @Test
    void isAllowed_shouldContinueRejectingRequestsAfterLimitIsExceeded() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMinutes(1);

        assertTrue(service.isAllowed("user-1", 1, window));
        assertFalse(service.isAllowed("user-1", 1, window));
        assertFalse(service.isAllowed("user-1", 1, window));
    }

    @Test
    void isAllowed_shouldTrackDifferentKeysIndependently() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMinutes(1);

        assertTrue(service.isAllowed("user-1", 1, window));
        assertFalse(service.isAllowed("user-1", 1, window));

        assertTrue(service.isAllowed("user-2", 1, window));
        assertFalse(service.isAllowed("user-2", 1, window));
    }

    @Test
    void isAllowed_shouldResetCountWhenWindowExpires() throws InterruptedException {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMillis(50);

        assertTrue(service.isAllowed("user-1", 1, window));

        assertFalse(service.isAllowed("user-1", 1, window));

        Thread.sleep(100);

        assertTrue(service.isAllowed("user-1", 1, window));
    }

    @Test
    void isAllowed_shouldCreateNewWindowForNewKey() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        Duration window = Duration.ofMinutes(1);

        assertTrue(service.isAllowed("user-1", 1, window));
        assertTrue(service.isAllowed("user-2", 1, window));
        assertTrue(service.isAllowed("user-3", 1, window));
    }

    @Test
    void isAllowed_shouldRespectZeroLimit() {

        InMemoryRateLimitService service =
                new InMemoryRateLimitService();

        boolean result = service.isAllowed(
                "user-1",
                0,
                Duration.ofMinutes(1)
        );

        assertFalse(result);
    }
}