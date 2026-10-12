package com.shopsphere.gateway;

import org.springframework.http.HttpMethod;

/** Decides which caller is allowed through the edge. Kept free of framework types so it is easy to test. */
public final class AccessPolicy {
    public enum Requirement { PUBLIC, USER, ADMIN }

    private AccessPolicy() { }

    public static Requirement requirementFor(HttpMethod method, String path) {
        if (path.startsWith("/api/carts") || path.startsWith("/api/orders") || path.startsWith("/api/notifications")
                || path.equals("/api/auth/me")) {
            return Requirement.USER;
        }
        boolean catalogue = path.startsWith("/api/products") || path.startsWith("/api/inventory");
        if (catalogue && method != HttpMethod.GET && method != HttpMethod.HEAD && method != HttpMethod.OPTIONS) {
            return Requirement.ADMIN;
        }
        return Requirement.PUBLIC;
    }
}
