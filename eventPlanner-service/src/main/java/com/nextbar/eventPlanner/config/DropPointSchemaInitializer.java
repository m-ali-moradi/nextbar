package com.nextbar.eventPlanner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures required drop_points schema columns exist for backward-compatible
 * upgrades on existing databases.
 */
@Component
@ConditionalOnProperty(value = "app.schema.drop-point-initializer.enabled", havingValue = "true", matchIfMissing = true)
public class DropPointSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DropPointSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DropPointSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureCapacityColumn();
    }

    private void ensureCapacityColumn() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = 'drop_points'
                """,
                Integer.class);

        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer columnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'drop_points'
                  AND column_name = 'capacity'
                """,
                Integer.class);

        if (columnCount != null && columnCount > 0) {
            return;
        }

        log.warn("Schema upgrade: adding missing column drop_points.capacity");
        jdbcTemplate.execute("ALTER TABLE drop_points ADD COLUMN capacity INT NOT NULL DEFAULT 100");
        log.info("Schema upgrade complete: drop_points.capacity added");
    }
}
