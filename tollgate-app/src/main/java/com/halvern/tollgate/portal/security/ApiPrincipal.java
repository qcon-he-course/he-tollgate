// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The haulier behind an API key.
 */
@Getter
@AllArgsConstructor
public class ApiPrincipal {

    private final Long haulierId;

    public String getName() {
        return "haulier:" + haulierId;
    }

    @Override
    public String toString() {
        return getName();
    }
}
