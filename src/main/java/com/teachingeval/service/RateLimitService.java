package com.teachingeval.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int limitPerMinute) {
        if (limitPerMinute <= 0) {
            return true;
        }
        long window = Instant.now().getEpochSecond() / 60;
        Bucket bucket = buckets.compute(key, (ignored, existing) -> {
            if (existing == null || existing.window() != window) {
                return new Bucket(window, new AtomicInteger(0));
            }
            return existing;
        });
        return bucket.count().incrementAndGet() <= limitPerMinute;
    }

    private record Bucket(long window, AtomicInteger count) {
    }
}
