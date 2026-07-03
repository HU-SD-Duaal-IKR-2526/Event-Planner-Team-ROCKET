package nl.hu.ikr.notification.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdempotencyGate {

    private final StringRedisTemplate redis;

    public IdempotencyGate(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean claim(String messageId) {

        Boolean isNew =
                redis.opsForValue()
                        .setIfAbsent(
                                "msg:" + messageId,
                                "1",
                                Duration.ofHours(24)
                        );

        return Boolean.TRUE.equals(isNew);
    }

    public void release(String messageId) {
        redis.delete("msg:" + messageId);
    }
}