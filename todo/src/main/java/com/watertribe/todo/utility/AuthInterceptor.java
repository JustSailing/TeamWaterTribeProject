package com.watertribe.todo.utility;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Intercepts incoming requests and validates the JWT from the Authorization header.
 * If the token is missing or invalid the request is rejected with a 401 Unauthorized.
 * If valid, the user's ID and username are attached to the request as attributes
 * so controllers can access them without re-parsing the token.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtility jwtUtility;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Allow CORS preflight requests through without authentication
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // A common strategy for passing auth tokens is through the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Expect header in the form: "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or malformed Authorization header");
            return false;
        }

        String token = authHeader.substring(7); // strip "Bearer "

        // Token is invalid - prevent the request from going through
        if (!jwtUtility.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return false;
        }

        // Token is valid — attach useful claims to the request for controllers to use
        String userId = jwtUtility.extractSubject(token);
        String username = jwtUtility.extractUsername(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true;
    }
}
