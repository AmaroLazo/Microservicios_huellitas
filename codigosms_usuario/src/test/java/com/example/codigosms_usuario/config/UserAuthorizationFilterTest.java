package com.example.codigosms_usuario.config;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAuthorizationFilterTest {
    private final JwtService jwt = new JwtService(new ObjectMapper(), Base64.getEncoder().encodeToString(new byte[32]));
    private User account;
    private int lookups;
    private final UserService users = new UserService(null, null, null) {
        @Override
        public User getProfile(Long id) {
            lookups++;
            return account;
        }
    };
    private final UserAuthorizationFilter filter = new UserAuthorizationFilter(jwt, users, "");

    @Test
    void customerCanReadOwnProfileButCannotReadAnotherProfileOrListUsers() throws Exception {
        account = User.builder().active(true).role(Role.CUSTOMER).build();
        String token = jwt.issue(10, Role.CUSTOMER);

        check("/api/users/10", token, 200);
        check("/api/users/11", token, 403);
        check("/api/users", token, 403);
    }

    @Test
    void adminCanListUsersAndRoleChangesInvalidateTheToken() throws Exception {
        account = User.builder().active(true).role(Role.ADMIN).build();
        String token = jwt.issue(10, Role.ADMIN);

        check("/api/users", token, 200);
        account.setRole(Role.CUSTOMER);
        check("/api/users", token, 401);
    }

    @Test
    void invalidSignatureIsRejected() throws Exception {
        String token = jwt.issue(10, Role.ADMIN);
        int signature = token.lastIndexOf('.') + 1;
        String invalid = token.substring(0, signature) + (token.charAt(signature) == 'A' ? 'B' : 'A')
                + token.substring(signature + 1);

        check("/api/users", invalid, 401);
        assertEquals(0, lookups);
    }

    private void check(String path, String token, int status) throws Exception {
        var request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();
        filter.doFilter(request, response, chain);

        assertEquals(status, response.getStatus());
        assertEquals(status == 200, chain.getRequest() == request);
    }
}
