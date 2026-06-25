package co.handk.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NormalUserStockPermissionRunner implements ApplicationRunner {
    private static final String NORMAL_USER_ROLE = "ROLE_NORMAL_USER";
    private static final List<String> REQUIRED_PERMISSION_CODES = List.of(
            "MENU_STOCK",
            "MENU_STOCK_SELF",
            "MENU_STOCK_A",
            "MENU_STOCK_B",
            "MENU_STOCK_C",
            "MENU_STOCK_CUSTOMER",
            "DATA_STOCK_SELF_READ",
            "DATA_STOCK_SELF_WRITE",
            "DATA_STOCK_A_READ",
            "DATA_STOCK_A_WRITE",
            "DATA_STOCK_B_READ",
            "DATA_STOCK_B_WRITE",
            "DATA_STOCK_C_READ",
            "DATA_STOCK_C_WRITE",
            "DATA_STOCK_CUSTOMER_READ",
            "DATA_STOCK_CUSTOMER_WRITE"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        try {
            Long roleId = findNormalUserRoleId();
            if (roleId == null) {
                log.info("normal user stock permission sync skipped: role {} not found", NORMAL_USER_ROLE);
                return;
            }
            int changed = 0;
            for (Long permissionId : findPermissionIds()) {
                changed += restoreOrInsert(roleId, permissionId);
            }
            if (changed > 0) {
                log.info("normal user stock permission sync completed, changed={}", changed);
            }
        } catch (Exception ex) {
            log.warn("normal user stock permission sync skipped", ex);
        }
    }

    private Long findNormalUserRoleId() {
        List<Long> rows = jdbcTemplate.queryForList(
                "SELECT id FROM t_role WHERE code = ? AND deleted = 0 AND status = 1 LIMIT 1",
                Long.class,
                NORMAL_USER_ROLE);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<Long> findPermissionIds() {
        String placeholders = String.join(",", REQUIRED_PERMISSION_CODES.stream().map(ignored -> "?").toList());
        Object[] params = REQUIRED_PERMISSION_CODES.toArray();
        return jdbcTemplate.queryForList(
                "SELECT id FROM t_permission WHERE deleted = 0 AND status = 1 AND code IN (" + placeholders + ")",
                Long.class,
                params);
    }

    private int restoreOrInsert(Long roleId, Long permissionId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int restored = jdbcTemplate.update("""
                UPDATE t_role_permission
                SET deleted = 0, updated_by = ?, update_time = ?
                WHERE role_id = ? AND permission_id = ?
                """, 1L, now, roleId, permissionId);
        if (restored > 0) {
            return restored;
        }
        return jdbcTemplate.update("""
                INSERT INTO t_role_permission
                    (role_id, permission_id, deleted, created_by, updated_by, create_time, update_time)
                VALUES (?, ?, 0, ?, ?, ?, ?)
                """, roleId, permissionId, 1L, 1L, now, now);
    }
}
