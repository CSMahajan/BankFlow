package com.bankflow.ratelimit;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimitService implements RateLimitService {


    private final ConcurrentHashMap<String, RateLimitEntry> requests = new ConcurrentHashMap<>();


    @Override
    public boolean isAllowed(String key, int limit, Duration window) {

        Instant now = Instant.now();

        RateLimitEntry entry = requests.compute(key, (k, existing) -> {

            if (existing == null || existing.isExpired(now, window)) {

                return new RateLimitEntry(1, now);
            }

            existing.increment();

            return existing;
        });


        return entry.getCount() <= limit;
    }


    @AllArgsConstructor
    private static class RateLimitEntry {

        private int count;

        private Instant windowStart;


        private void increment() {
            count++;
        }


        private boolean isExpired(Instant now, Duration window) {
            return !windowStart.plus(window).isAfter(now);
        }


        private int getCount() {
            return count;
        }
    }
}