package nl.teamrocket.core.shared;

import java.util.UUID;

/**
 * Thread-local correlation ID — shared across all modules in the Core Monolith.
 * Set by CorrelationFilter on every incoming HTTP request.
 */
public final class CorrelationContext {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();
    private CorrelationContext() {}
    public static void set(String id) { HOLDER.set(id); }
    public static String current() { String id = HOLDER.get(); return id != null ? id : UUID.randomUUID().toString(); }
    public static void clear() { HOLDER.remove(); }
}
