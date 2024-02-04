package org.example.config.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HealthIndicatorImpl implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public HealthIndicatorImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up().withDetail("Database", "Connection is OK").build();
        } catch (Exception e) {
            return Health.down().withDetail("Database", "Connection error: " + e.getMessage()).build();
        }
    }
}
