-- Remove legacy menu visibility config that is no longer used for permission scope.

DELETE FROM t_config
WHERE name IN (
    'perm.group.menu.json',
    'perm.group.menu.A',
    'perm.group.menu.B',
    'perm.group.menu.C'
);



INSERT INTO t_config (`name`, `group`, `title`, `tip`, `type`, `value`, `content`, `created_by`, `updated_by`)
VALUES
    ('stock.group.codes', 'stock', 'Stock group codes', 'Department codes allowed to own group stock', 'string', 'A,B,C', NULL, 1, 1),
    ('perm.group.menu.A', 'permission', 'Group A menu scope', 'Menu codes visible to department A', 'string', 'stock,stockA,stockOrder,stockType,stockRecord,priceRecord', NULL, 1, 1),
    ('perm.group.menu.B', 'permission', 'Group B menu scope', 'Menu codes visible to department B', 'string', 'stock,stockB,stockOrder,stockType,stockRecord,priceRecord', NULL, 1, 1),
    ('perm.group.menu.C', 'permission', 'Group C menu scope', 'Menu codes visible to department C', 'string', 'stock,stockC,stockOrder,stockType,stockRecord,priceRecord', NULL, 1, 1)
ON DUPLICATE KEY UPDATE
                     `group` = VALUES(`group`),
                     title = VALUES(title),
                     tip = VALUES(tip),
                     type = VALUES(type),
                     value = VALUES(value),
                     content = VALUES(content),
                     updated_by = VALUES(updated_by),
                     update_time = NOW();

SELECT rp.role_id, rp.permission_id, p.code, p.path, p.type
FROM t_role_permission rp
         JOIN t_permission p ON p.id = rp.permission_id
WHERE rp.role_id = 2
  AND rp.deleted = 0
  AND p.deleted = 0
  AND p.code IN ('MENU_GOODS', 'DATA_GOODS_READ', 'DATA_GOODS_WRITE');



INSERT INTO t_role_permission
(role_id, permission_id, deleted, created_by, updated_by, create_time, update_time)
SELECT 2, p.id, 0, 1, 1, NOW(), NOW()
FROM t_permission p
WHERE p.deleted = 0
  AND p.status = 1
  AND p.type = 2
  AND p.code IN (
                 'DATA_GOODS_READ',
                 'DATA_GOODS_SKU_READ',
                 'DATA_GOODS_IMAGE_READ',
                 'DATA_GOODS_SKU_SPEC_READ',
                 'DATA_STOCK_READ',
                 'DATA_STOCK_READ',
                 'DATA_STOCK_A_READ',
                 'DATA_STOCK_B_READ',
                 'DATA_STOCK_C_READ',
                 'DATA_STOCK_ORDER_READ',
                 'DATA_STOCK_ORDER_ITEM_READ',
                 'DATA_STOCK_TYPE_READ',
                 'DATA_STOCK_RECORD_READ',
                 'DATA_PRICE_RECORD_READ',
                 'DATA_REQUEST_FORM_READ',
                 'DATA_REQUEST_ITEM_READ',
                 'DATA_CUSTOMER_READ',
                 'DATA_CUSTOMER_LEVEL_READ',
                 'DATA_USER_READ',
                 'DATA_DEPT_READ',
                 'DATA_WAREHOUSE_READ',
                 'DATA_ROLE_READ',
                 'DATA_PERMISSION_READ',
                 'DATA_MESSAGE_READ'
    )
  AND NOT EXISTS (
    SELECT 1
    FROM t_role_permission rp
    WHERE rp.role_id = 2
      AND rp.permission_id = p.id
      AND rp.deleted = 0
);



INSERT INTO t_role_permission
(role_id, permission_id, deleted, created_by, updated_by, create_time, update_time)
SELECT 2, p.id, 0, 1, 1, NOW(), NOW()
FROM t_permission p
WHERE p.deleted = 0
  AND p.status = 1
  AND p.type = 2
  AND p.code LIKE '%_READ'
  AND NOT EXISTS (
    SELECT 1
    FROM t_role_permission rp
    WHERE rp.role_id = 2
      AND rp.permission_id = p.id
      AND rp.deleted = 0
);


SELECT rp.role_id, rp.permission_id, p.code, p.path, p.type
FROM t_role_permission rp
         JOIN t_permission p ON p.id = rp.permission_id
WHERE rp.role_id = 2
  AND rp.deleted = 0
  AND p.deleted = 0
  AND p.code = 'DATA_GOODS_READ';

INSERT INTO t_role_permission
(role_id, permission_id, deleted, created_by, updated_by, create_time, update_time)
SELECT 2, id, 0, 1, 1, NOW(), NOW()
FROM t_permission
WHERE code = 'DATA_GOODS_READ'
  AND deleted = 0
LIMIT 1;



INSERT INTO t_role_permission
(role_id, permission_id, deleted, created_by, updated_by, create_time, update_time)
SELECT 2, p.id, 0, 1, 1, NOW(), NOW()
FROM t_permission p
WHERE p.deleted = 0
  AND p.code IN (
                 'DATA_GOODS_READ',
                 'DATA_GOODS_SKU_READ',
                 'DATA_GOODS_IMAGE_READ',
                 'DATA_GOODS_SKU_SPEC_READ',
                 'DATA_STOCK_READ',
                 'DATA_STOCK_SELF_READ',
                 'DATA_STOCK_A_READ',
                 'DATA_STOCK_B_READ',
                 'DATA_STOCK_C_READ',
                 'DATA_STOCK_ORDER_READ',
                 'DATA_STOCK_ORDER_ITEM_READ',
                 'DATA_STOCK_TYPE_READ',
                 'DATA_STOCK_RECORD_READ',
                 'DATA_PRICE_RECORD_READ',
                 'DATA_REQUEST_FORM_READ',
                 'DATA_REQUEST_ITEM_READ',
                 'DATA_CUSTOMER_READ',
                 'DATA_CUSTOMER_LEVEL_READ',
                 'DATA_USER_READ',
                 'DATA_DEPT_READ',
                 'DATA_WAREHOUSE_READ',
                 'DATA_ROLE_READ',
                 'DATA_PERMISSION_READ',
                 'DATA_MESSAGE_READ'
    )
  AND NOT EXISTS (
    SELECT 1
    FROM t_role_permission rp
    WHERE rp.role_id = 2
      AND rp.permission_id = p.id
      AND rp.deleted = 0
);




SELECT rp.role_id, rp.permission_id, p.code
FROM t_role_permission rp
         JOIN t_permission p ON p.id = rp.permission_id
WHERE rp.role_id = 2
  AND rp.deleted = 0
  AND p.deleted = 0
  AND p.code = 'DATA_GOODS_READ';


SELECT rp.role_id, rp.permission_id, rp.deleted AS rp_deleted,
       p.code, p.deleted AS p_deleted, p.status, p.type
FROM t_role_permission rp
         JOIN t_permission p ON p.id = rp.permission_id
WHERE rp.role_id = 2
  AND rp.deleted = 0
  AND p.deleted = 0
  AND p.status = 1
  AND p.code = 'DATA_GOODS_READ';


SELECT ur.user_id, ur.role_id, r.code, r.deleted, r.status
FROM t_user_role ur
         JOIN t_role r ON r.id = ur.role_id
WHERE ur.user_id = 2
  AND ur.deleted = 0
  AND r.deleted = 0
  AND r.status = 1;
