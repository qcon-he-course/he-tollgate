// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * API keys, looked up on every portal request.
 *
 * @author Marco Bellini
 */
@Repository
public class ApiKeyDao {

    private static final String FIND_ACTIVE =
            "SELECT id, haulier_id, api_key, label, active, created_at, last_used_at FROM api_key WHERE api_key = ? AND active";

    private static final String TOUCH = "UPDATE api_key SET last_used_at = now() WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ApiKey findActive(String key) {
        List<ApiKey> keys = jdbcTemplate.query(FIND_ACTIVE, (rs, i) -> {
            ApiKey k = new ApiKey();
            k.setId(rs.getLong("id"));
            k.setHaulierId(rs.getLong("haulier_id"));
            k.setApiKey(rs.getString("api_key"));
            k.setLabel(rs.getString("label"));
            k.setActive(rs.getBoolean("active"));
            k.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return k;
        }, key);
        return keys.isEmpty() ? null : keys.get(0);
    }

    public void touch(Long id) {
        jdbcTemplate.update(TOUCH, id);
    }
}
