package com.example.codigosms_usuario.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.service.UserService;

@Component
public class UserAuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserService users;
    private final String internalKey;

    public UserAuthorizationFilter(JwtService jwt, UserService users,
            @Value("${HUELLITAS_INTERNAL_KEY:}") String internalKey) {
        this.jwt = jwt;
        this.users = users;
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
        if (!(path.equals("/api/users") || path.startsWith("/api/users/")) || method.equals("OPTIONS")
                || (method.equals("GET") && path.equals("/api/users/regiones"))
                || (method.equals("POST") && (path.equals("/api/users") || path.equals("/api/users/login")
                || path.equals("/api/users/password-reset/request") || path.equals("/api/users/password-reset/confirm")))) {
            chain.doFilter(request, response);
            return;
        }

        String suppliedKey = request.getHeader("X-Huellitas-Internal");
        if (path.equals("/api/users") && method.equals("GET") && !internalKey.isBlank() && suppliedKey != null
                && MessageDigest.isEqual(internalKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }

        JwtService.Identity identity;
        try {
            identity = jwt.verify(request.getHeader("Authorization"));
            User user = users.getProfile(identity.userId());
            if (!Boolean.TRUE.equals(user.getActive()) || user.getRole() != identity.role()) {
                throw new IllegalArgumentException("Sesión desactualizada");
            }
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            response.sendError(401, "Token inválido o vencido. Inicia sesión nuevamente.");
            return;
        }

        if (!isAllowed(path, method, identity)) {
            response.sendError(403, "No tienes permiso para esta operación.");
            return;
        }
        request.setAttribute("huellitasIdentity", identity);
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String path, String method, JwtService.Identity identity) {
        if (method.equals("GET") && path.equals("/api/users/session")) return true;
        if (method.equals("POST") && path.equals("/api/users/logout")) return true;

        boolean admin = identity.role() == Role.ADMIN;
        if (method.equals("GET") && (path.equals("/api/users") || path.equals("/api/users/search"))) {
            return admin;
        }
        String own = "/api/users/" + identity.userId();
        if (path.equals(own + "/password") || path.equals(own + "/deactivate")) {
            return !admin || path.endsWith("/password");
        }
        if (path.equals(own + "/role") || path.equals(own + "/toggle-status")) return false;
        if (path.matches("/api/users/[0-9]+/(role|toggle-status|administracion)")) return admin;
        if (path.matches("/api/users/[0-9]+(/addresses(/[0-9]+)?)?")) {
            return admin || path.equals(own) || path.startsWith(own + "/addresses");
        }
        return false;
    }
}
