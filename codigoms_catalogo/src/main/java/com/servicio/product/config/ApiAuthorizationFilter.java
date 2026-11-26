package com.servicio.product.config;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiAuthorizationFilter extends OncePerRequestFilter {
    private final RestClient users;
    private final String internalKey;

    public ApiAuthorizationFilter(@Value("${HUELLITAS_USERS_URL:http://localhost:8090}") String usersUrl,
            @Value("${HUELLITAS_INTERNAL_KEY:}") String internalKey) {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.users = RestClient.builder().baseUrl(usersUrl).requestFactory(factory).build();
        this.internalKey = internalKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String rawPath = request.getRequestURI();
        if (rawPath.indexOf(';') >= 0 || path.indexOf(';') >= 0) {
            response.sendError(400, "Ruta inválida.");
            return;
        }
        String method = request.getMethod();
        if (!isApiPath(path) || method.equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        boolean publicRead = method.equals("GET") && (path.equals("/api/v1/products/available")
                || path.matches("/api/v1/products/[0-9]+") || path.equals("/api/v1/categories"));
        if (publicRead && authorization == null) {
            chain.doFilter(request, response);
            return;
        }

        if ((method.equals("POST") && path.matches("/api/v1/products/[0-9]+/stock")) && hasInternalKey(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(401, "Inicia sesión para continuar.");
            return;
        }

        Identity identity;
        try {
            identity = users.get().uri("/api/users/session").header("Authorization", authorization)
                    .retrieve().body(Identity.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            response.sendError(401, "Token inválido o vencido. Inicia sesión nuevamente.");
            return;
        } catch (RestClientException e) {
            response.sendError(503, "No se pudo verificar la sesión con Usuarios.");
            return;
        }

        if (identity == null || identity.userId() <= 0
                || !("CUSTOMER".equals(identity.role()) || identity.isAdmin())) {
            response.sendError(401, "Sesión inválida.");
            return;
        }

        if (!publicRead && !identity.isAdmin()) {
            response.sendError(403, "No tienes permiso para esta operación.");
            return;
        }
        request.setAttribute("huellitasUserId", identity.userId());
        chain.doFilter(request, response);
    }

    private boolean isApiPath(String path) {
        return path.equals("/api/v1/products") || path.startsWith("/api/v1/products/")
                || path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories/");
    }

    private boolean hasInternalKey(HttpServletRequest request) {
        String suppliedKey = request.getHeader("X-Huellitas-Internal");
        return !internalKey.isBlank() && suppliedKey != null
                && MessageDigest.isEqual(internalKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8));
    }

    public record Identity(long userId, String role) {
        boolean isAdmin() {
            return "ADMIN".equals(role);
        }
    }
}
