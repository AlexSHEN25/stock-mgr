SET FOREIGN_KEY_CHECKS = 0;

-- Remove obsolete "まとめ納品書明細" menu permissions.
DELETE rp
FROM t_role_permission rp
JOIN t_permission p ON p.id = rp.permission_id
WHERE p.code IN ('DATA_REQUEST_ITEM_READ', 'DATA_REQUEST_ITEM_WRITE');

DELETE FROM t_permission
WHERE code IN ('DATA_REQUEST_ITEM_READ', 'DATA_REQUEST_ITEM_WRITE');

-- Keep ROLE_NORMAL_USER aligned with the latest menu set.
DELETE rp
FROM t_role_permission rp
JOIN t_role r ON r.id = rp.role_id
WHERE r.code = 'ROLE_NORMAL_USER';

INSERT INTO t_role_permission (
    role_id,
    permission_id,
    deleted,
    created_by,
    updated_by,
    create_time,
    update_time
)
SELECT
    r.id,
    p.id,
    0,
    1,
    1,
    NOW(),
    NOW()
FROM t_role r
JOIN t_permission p
WHERE r.code = 'ROLE_NORMAL_USER'
  AND r.deleted = 0
  AND p.deleted = 0
  AND p.status = 1
  AND (
      p.type <> 2
      OR RIGHT(p.code, 5) = '_READ'
      OR (
          RIGHT(p.code, 6) = '_WRITE'
          AND (
              p.path = '/api/stockOrder' OR p.path LIKE '/api/stockOrder/%'
              OR p.path = '/api/stockOrderItem' OR p.path LIKE '/api/stockOrderItem/%'
              OR p.path = '/api/requestForm' OR p.path LIKE '/api/requestForm/%'
              OR p.path = '/api/stock' OR p.path LIKE '/api/stock/%'
          )
      )
  );

SET FOREIGN_KEY_CHECKS = 1;
