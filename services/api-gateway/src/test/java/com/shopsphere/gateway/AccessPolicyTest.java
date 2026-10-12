package com.shopsphere.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopsphere.gateway.AccessPolicy.Requirement;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class AccessPolicyTest {
    @Test
    void browsingTheCatalogueIsPublic() {
        assertThat(AccessPolicy.requirementFor(HttpMethod.GET, "/api/products")).isEqualTo(Requirement.PUBLIC);
        assertThat(AccessPolicy.requirementFor(HttpMethod.GET, "/api/inventory")).isEqualTo(Requirement.PUBLIC);
        assertThat(AccessPolicy.requirementFor(HttpMethod.POST, "/api/auth/login")).isEqualTo(Requirement.PUBLIC);
    }

    @Test
    void personalDataNeedsASignedInUser() {
        assertThat(AccessPolicy.requirementFor(HttpMethod.GET, "/api/carts")).isEqualTo(Requirement.USER);
        assertThat(AccessPolicy.requirementFor(HttpMethod.POST, "/api/orders")).isEqualTo(Requirement.USER);
        assertThat(AccessPolicy.requirementFor(HttpMethod.GET, "/api/auth/me")).isEqualTo(Requirement.USER);
    }

    @Test
    void catalogueChangesNeedAnAdmin() {
        assertThat(AccessPolicy.requirementFor(HttpMethod.POST, "/api/products")).isEqualTo(Requirement.ADMIN);
        assertThat(AccessPolicy.requirementFor(HttpMethod.PUT, "/api/inventory/SKU-1001")).isEqualTo(Requirement.ADMIN);
    }
}
