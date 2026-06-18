package nl.teamrocket.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.identity.application.handler.RefreshTokenHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that checks the jti (JWT ID) claim against the token blacklist.
 * Runs after authentication so the JWT is already parsed and the jti is available.
 *
 * If the token is blacklisted (user logged out or password was changed),
 * the request is rejected with 401 even though the JWT signature is valid.
 *
 * Architecture doc requirement: "token-blacklist" as an Identity responsibility.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter extends OncePerRequestFilter {

    private final RefreshTokenHandler refreshTokenHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String jti = jwt.getId();

            if (jti != null && refreshTokenHandler.isBlacklisted(jti)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"token_revoked\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
