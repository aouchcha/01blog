package _blog.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 1;
    private static final long WINDOW_MS = 1000;

    private final Map<String, UserRate> userRates = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        long now = Instant.now().toEpochMilli();
        userRates.putIfAbsent(username, new UserRate(0, now));

        UserRate rate = userRates.get(username);

        synchronized (rate) { // prevent race conditions
            if (now - rate.timestamp > WINDOW_MS) {
                rate.count = 1;
                rate.timestamp = now;
                return true;
            } else {
                if (rate.count < MAX_REQUESTS) {
                    rate.count++;
                    return true;
                } else {
                    return false;
                }
            }
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

