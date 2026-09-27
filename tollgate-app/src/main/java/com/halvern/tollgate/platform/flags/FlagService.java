package com.halvern.tollgate.platform.flags;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Feature flags from the feature_flag table (CR-118).
 * Unknown flags are off. If the table can't be read the flag is off, so a missing row never breaks a request.
 */
@Service
public class FlagService {

    private static final Logger log = LoggerFactory.getLogger(FlagService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // CR-131: own transaction so a failed lookup does not roll back the caller
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean isEnabled(String name) {
        try {
            List<Boolean> rows = jdbcTemplate.queryForList("select enabled from feature_flag where name = ?", Boolean.class, name);
            return !rows.isEmpty() && Boolean.TRUE.equals(rows.get(0));
        } catch (Exception e) {
            log.error("Could not read feature flag " + name, e);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<java.util.Map<String, Object>> findAll() {
        try {
            return jdbcTemplate.queryForList("select name, enabled, description, updated_at, updated_by from feature_flag order by name");
        } catch (Exception e) {
            log.error("Could not read feature flags", e);
            return new java.util.ArrayList<>();
        }
    }
}
