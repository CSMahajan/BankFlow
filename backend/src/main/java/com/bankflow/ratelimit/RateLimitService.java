package com.bankflow.ratelimit;

import java.time.Duration;

public interface RateLimitService {

    boolean isAllowed(
            String key,
            int limit,
            Duration window
    );
}