package nl.teamrocket.core.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String cid = Optional.ofNullable(req.getHeader(HEADER))
                .filter(h -> !h.isBlank()).orElse(UUID.randomUUID().toString());
        CorrelationContext.set(cid);
        MDC.put("correlationId", cid);
        try {
            res.setHeader(HEADER, cid);
            chain.doFilter(req, res);
        } finally {
            MDC.remove("correlationId");
            CorrelationContext.clear();
        }
    }
}
