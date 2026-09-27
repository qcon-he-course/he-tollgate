package com.halvern.tollgate.platform.settings;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SettingsDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<SystemSetting> findAll() {
        return jdbcTemplate.query("select setting_key, setting_value, description, updated_at, updated_by from system_setting order by setting_key",
                (rs, i) -> {
                    SystemSetting s = new SystemSetting();
                    s.setKey(rs.getString("setting_key"));
                    s.setValue(rs.getString("setting_value"));
                    s.setDescription(rs.getString("description"));
                    s.setUpdatedAt(rs.getTimestamp("updated_at"));
                    s.setUpdatedBy(rs.getString("updated_by"));
                    return s;
                });
    }

    public void update(String key, String value, String username) {
        jdbcTemplate.update("update system_setting set setting_value = ?, updated_at = now(), updated_by = ? where setting_key = ?",
                value, username, key);
    }
}
