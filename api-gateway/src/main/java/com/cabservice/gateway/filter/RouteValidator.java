package com.cabservice.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Route Validator
 * Determines which endpoints require authentication and which are public.
 */
@Component
public class RouteValidator {

    /**
     * List of public endpoints that don't require authentication
     */
    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/users/register",
            "/api/users/login",
            "/api/users/verify-email",
            "/api/users/verify-phone",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/users/refresh-token",
            "/eureka",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    /**
     * Predicate to check if a request requires authentication
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> PUBLIC_ENDPOINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
