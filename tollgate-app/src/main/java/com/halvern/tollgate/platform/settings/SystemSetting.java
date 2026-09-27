package com.halvern.tollgate.platform.settings;

import lombok.Data;

@Data
public class SystemSetting {
    private String key;
    private String value;
    private String description;
    private java.sql.Timestamp updatedAt;
    private String updatedBy;
}
