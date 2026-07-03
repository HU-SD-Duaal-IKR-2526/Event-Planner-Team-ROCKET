package nl.teamrocket.identity.security;

import java.util.UUID;

/**
 * Holds the current request's correlation-id in a ThreadLocal.
 * Set by CorrelationFilter on every incoming HTTP request.
 * Used by handlers and event publishers to stamp domain events.
 */
public final class CorrelationContext {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationContext() {}

    public static void set(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static String current() {
        String id = HOLDER.get();
        return id != null ? id : UUID.randomUUID().toString();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
