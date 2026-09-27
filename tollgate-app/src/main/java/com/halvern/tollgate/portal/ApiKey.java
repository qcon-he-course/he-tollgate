// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * An API key issued to a haulier for the portal.
 */
@Data
public class ApiKey {

    private Long id;
    private Long haulierId;
    private String apiKey;
    private String label;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
