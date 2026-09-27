// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import com.halvern.tollgate.portal.ApiKey;
import com.halvern.tollgate.portal.ApiKeyDao;

/**
 * Authenticates portal requests to /api/** with the X-Api-Key header (PI-12).
 *
 * @author Marco Bellini
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Api-Key";

    private static final UrlPathHelper PATHS = new UrlPathHelper();

    private final ApiKeyDao apiKeyDao;

    public ApiKeyAuthFilter(ApiKeyDao apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PATHS.getPathWithinApplication(request).startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        ApiKey apiKey = key == null ? null : apiKeyDao.findActive(key.trim());
        if (apiKey == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid API key\"}");
            return;
        }
        apiKeyDao.touch(apiKey.getId());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                new ApiPrincipal(apiKey.getHaulierId()), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_HAULIER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }

    public static Long currentHaulierId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((ApiPrincipal) principal).getHaulierId();
    }

    public static String currentActor() {
        return "haulier:" + currentHaulierId();
    }
}
