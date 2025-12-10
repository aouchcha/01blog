package _blog.backend.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterService {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 1000;

    private final Map<String, UserRate> userRates = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        long now = Instant.now().toEpochMilli();
        
        UserRate rate = userRates.compute(username, (key, existingRate) -> {
            if (existingRate == null) {
                return new UserRate(1, now);
            }
            
            synchronized (existingRate) {
                if (now - existingRate.timestamp > WINDOW_MS) {
                    existingRate.count = 1;
                    existingRate.timestamp = now;
                } else if (existingRate.count < MAX_REQUESTS) {
                    existingRate.count++;
                }
                return existingRate;
            }
        });
        
        synchronized (rate) {
            return rate.count <= MAX_REQUESTS;
        }
    }

    private static class UserRate {
        int count;
        long timestamp;

        UserRate(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}