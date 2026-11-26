package com.servicio.codigoms_pagos.config;

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

import com.servicio.codigoms_pagos.client.OrderClient;
import com.servicio.codigoms_pagos.repository.PaymentRepository;
import feign.FeignException;

@Component
public class ApiAuthorizationFilter extends OncePerRequestFilter {
    private final RestClient users;
    private final String internalKey;
    private final PaymentRepository payments;
    private final OrderClient orders;

    public ApiAuthorizationFilter(@Value("${HUELLITAS_USERS_URL:http://localhost:8090}") String usersUrl,
            @Value("${HUELLITAS_INTERNAL_KEY:}") String internalKey, PaymentRepository payments, OrderClient orders) {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.users = RestClient.builder().baseUrl(usersUrl).requestFactory(factory).build();
        this.internalKey = internalKey;
        this.payments = payments;
        this.orders = orders;
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

        if ((method.equals("POST") && path.equals("/api/webhooks/payments")) && hasInternalKey(request)) {
            chain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
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

        boolean allowed;
        try {
            allowed = isAllowed(path, method, identity);
        } catch (NumberFormatException e) {
            response.sendError(400, "Identificador inválido.");
            return;
        } catch (FeignException e) {
            response.sendError(503, "No se pudo verificar el pedido.");
            return;
        }
        if (!allowed) {
            response.sendError(403, "No tienes permiso para esta operación.");
            return;
        }
        request.setAttribute("huellitasUserId", identity.userId());
        chain.doFilter(request, response);
    }

    private boolean isApiPath(String path) {
        return path.equals("/api/payments") || path.startsWith("/api/payments/")
                || path.equals("/api/webhooks") || path.startsWith("/api/webhooks/");
    }

    private boolean hasInternalKey(HttpServletRequest request) {
        String suppliedKey = request.getHeader("X-Huellitas-Internal");
        return !internalKey.isBlank() && suppliedKey != null
                && MessageDigest.isEqual(internalKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isAllowed(String path, String method, Identity identity) {
        if (path.startsWith("/api/webhooks")) return false;
        if (method.equals("POST") && path.equals("/api/payments")) return !identity.isAdmin();
        if (identity.isAdmin()) return true;
        if (method.equals("GET") && path.matches("/api/payments/by-order/[0-9]+")) {
            long orderId = Long.parseLong(path.substring("/api/payments/by-order/".length()));
            return ownsOrder(orderId, identity.userId());
        }
        if ((method.equals("GET") && path.matches("/api/payments/[0-9]+/(status|receipt)"))
                || (method.equals("POST") && path.matches("/api/payments/[0-9]+/retry"))) {
            long paymentId = Long.parseLong(path.split("/")[3]);
            return payments.findById(paymentId).filter(payment -> ownsOrder(payment.getOrderId(), identity.userId()))
                    .isPresent();
        }
        return false;
    }

    public boolean ownsOrder(long orderId, long userId) {
        try {
            var order = orders.getOrder(orderId);
            return order != null && Long.valueOf(userId).equals(order.getUserId());
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    public record Identity(long userId, String role) {
        boolean isAdmin() {
            return "ADMIN".equals(role);
        }
    }
}
