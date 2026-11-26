package com.servicio.product.config;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiAuthorizationFilterTest {
    @Test
    void permiteInvitadosEnCatalogoYValidaLosTokensPresentados() throws Exception {
        var usuarios = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        usuarios.createContext("/api/users/session", request -> {
            try (request) {
                String token = request.getRequestHeaders().getFirst("Authorization");
                String role = switch (token) {
                    case "Bearer cliente" -> "CUSTOMER";
                    case "Bearer administrador" -> "ADMIN";
                    default -> null;
                };
                if (role == null) {
                    request.sendResponseHeaders(401, -1);
                    return;
                }
                byte[] body = ("{\"userId\":1,\"role\":\"" + role + "\"}").getBytes(StandardCharsets.UTF_8);
                request.getResponseHeaders().set("Content-Type", "application/json");
                request.sendResponseHeaders(200, body.length);
                request.getResponseBody().write(body);
            }
        });
        usuarios.start();
        try {
            var filter = new ApiAuthorizationFilter("http://127.0.0.1:" + usuarios.getAddress().getPort(), "");
            assertStatus(filter, "/api/v1/products/available", null, 200);
            assertStatus(filter, "/api/v1/products/available", "Bearer invalido", 401);
            assertStatus(filter, "/api/v1/products/available", "Bearer cliente", 200);
            assertStatus(filter, "/api/v1/products", null, 401);
            assertStatus(filter, "/api/v1/products", "Bearer cliente", 403);
            assertStatus(filter, "/api/v1/products", "Bearer administrador", 200);
        } finally {
            usuarios.stop(0);
        }
    }

    private void assertStatus(ApiAuthorizationFilter filter, String path, String token, int status) throws Exception {
        var request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        if (token != null) request.addHeader("Authorization", token);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(status, response.getStatus());
        assertEquals(status == 200, chain.getRequest() == request);
    }
}
