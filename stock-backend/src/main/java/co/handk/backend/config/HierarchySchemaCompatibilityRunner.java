package co.handk.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class HierarchySchemaCompatibilityRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public HierarchySchemaCompatibilityRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureSeriesBrandId();
        ensureMakerSeriesId();
    }

    private void ensureSeriesBrandId() {
        if (hasColumn("t_series", "brand_id")) {
            return;
        }
        log.info("Adding missing column t_series.brand_id for brand -> series hierarchy");
        jdbcTemplate.execute("ALTER TABLE t_series ADD COLUMN brand_id BIGINT NULL AFTER english_name");
        jdbcTemplate.execute("CREATE INDEX idx_t_series_brand_id ON t_series (brand_id)");
        if (hasTable("t_series_brand_relation")) {
            jdbcTemplate.execute("""
                    UPDATE t_series s
                    JOIN (
                        SELECT series_id, MIN(brand_id) AS brand_id
                        FROM t_series_brand_relation
                        WHERE deleted = 0
                        GROUP BY series_id
                    ) rel ON rel.series_id = s.id
                    SET s.brand_id = rel.brand_id
                    WHERE s.brand_id IS NULL
                    """);
        }
        if (hasTable("t_brand_series_maker_relation")) {
            jdbcTemplate.execute("""
                    UPDATE t_series s
                    JOIN (
                        SELECT series_id, MIN(brand_id) AS brand_id
                        FROM t_brand_series_maker_relation
                        WHERE deleted = 0
                        GROUP BY series_id
                    ) rel ON rel.series_id = s.id
                    SET s.brand_id = rel.brand_id
                    WHERE s.brand_id IS NULL
                    """);
        }
    }

    private void ensureMakerSeriesId() {
        if (hasColumn("t_maker", "series_id")) {
            return;
        }
        log.info("Adding missing column t_maker.series_id for series -> maker hierarchy");
        jdbcTemplate.execute("ALTER TABLE t_maker ADD COLUMN series_id BIGINT NULL AFTER english_name");
        jdbcTemplate.execute("CREATE INDEX idx_t_maker_series_id ON t_maker (series_id)");
        if (hasTable("t_brand_series_maker_relation")) {
            jdbcTemplate.execute("""
                    UPDATE t_maker m
                    JOIN (
                        SELECT maker_id, MIN(series_id) AS series_id
                        FROM t_brand_series_maker_relation
                        WHERE deleted = 0
                        GROUP BY maker_id
                    ) rel ON rel.maker_id = m.id
                    SET m.series_id = rel.series_id
                    WHERE m.series_id IS NULL
                    """);
        }
    }

    private boolean hasTable(String tableName) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, tableName, null)) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to inspect table metadata for " + tableName, ex);
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Failed to inspect column metadata for " + tableName + "." + columnName, ex);
        }
    }
}
