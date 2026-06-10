SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Import script for data/test-data/delivery_split_2026.sql
-- Usage:
-- 1. Run delivery_split_2026.sql first to create delivery_split_test.
-- 2. Run this file to generate normalized stock split data.
--
-- Mapping rule:
-- - delivery_split_test.item_code -> t_goods_sku.sku_code
-- - t_goods_sku -> t_goods -> related master data
-- - a_split / b_split / c_split become group stock allocations
-- - qty_delivered becomes outbound/arrival quantity basis
--
-- This script keeps data idempotent by removing previously imported rows for the same source rows.

DELETE gs
FROM t_group_stock gs
JOIN t_stock_batch sb ON sb.id = gs.batch_id
JOIN t_stock_order_item soi ON soi.id = sb.inbound_order_item_id
WHERE soi.remark LIKE 'delivery_split_2026 row:%';

DELETE sb
FROM t_stock_batch sb
JOIN t_stock_order_item soi ON soi.id = sb.inbound_order_item_id
WHERE soi.remark LIKE 'delivery_split_2026 row:%';

DELETE soi
FROM t_stock_order_item soi
JOIN t_stock_order so ON so.id = soi.order_id
WHERE so.remark LIKE 'delivery_split_2026 row:%';

DELETE so
FROM t_stock_order so
WHERE so.remark LIKE 'delivery_split_2026 row:%';

SET @base_order_no := 'DS2026';

-- Create missing master data for item_code -> sku_code.
-- Default mapping uses the "other" brand/category that already exists in the seed data.
INSERT INTO t_goods
(
    name, english_name, brand_id, series_id, category_id, maker_id, description, is_hot, status, sort,
    deleted, created_by, updated_by, create_time, update_time
)
SELECT
    CONCAT('delivery_split_2026-', t.item_code) AS name,
    NULLIF(t.item_name_en, '') AS english_name,
    9 AS brand_id,
    NULL AS series_id,
    5 AS category_id,
    NULL AS maker_id,
    CONCAT('delivery_split_2026 item_code=', t.item_code) AS description,
    0 AS is_hot,
    1 AS status,
    0 AS sort,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM (
    SELECT DISTINCT item_code, item_name_jp, item_name_en
    FROM delivery_split_test
) t
LEFT JOIN t_goods_sku sku ON sku.sku_code = t.item_code AND sku.deleted = 0
LEFT JOIN t_goods g ON g.id = sku.goods_id AND g.deleted = 0
WHERE sku.id IS NULL;

INSERT INTO t_goods_sku
(
    goods_id, sku_code, sku_name, price, currency, cost_price, update_price, price_update_time, barcode,
    weight, volume, status, deleted, created_by, updated_by, create_time, update_time
)
SELECT
    g.id AS goods_id,
    t.item_code AS sku_code,
    COALESCE(NULLIF(t.item_name_en, ''), NULLIF(t.item_name_jp, ''), t.item_code) AS sku_name,
    0.00 AS price,
    'JPY' AS currency,
    0.00 AS cost_price,
    NULL AS update_price,
    NULL AS price_update_time,
    NULL AS barcode,
    NULL AS weight,
    NULL AS volume,
    1 AS status,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM (
    SELECT DISTINCT item_code, item_name_jp, item_name_en
    FROM delivery_split_test
) t
JOIN t_goods g
    ON g.deleted = 0
   AND g.description = CONCAT('delivery_split_2026 item_code=', t.item_code)
LEFT JOIN t_goods_sku sku ON sku.sku_code = t.item_code AND sku.deleted = 0
WHERE sku.id IS NULL;

INSERT INTO t_stock_order
(
    order_no, order_type, warehouse_id, source_type, source_id, total_qty, stock_type_id, state,
    requester_id, requester_name, operator_id, operator_name, remark, approver_id, approver_name,
    approve_time, biz_date, version, finish_time, outbound_mode, customer_id, customer_name,
    dept_id, dept_code, sale_deadline, deleted, created_by, updated_by, create_time, update_time
)
SELECT
    CONCAT(@base_order_no, '-', LPAD(t.row_no, 5, '0')) AS order_no,
    1 AS order_type,
    1 AS warehouse_id,
    4 AS source_type,
    t.row_no AS source_id,
    COALESCE(t.qty_delivered, 0) AS total_qty,
    NULL AS stock_type_id,
    2 AS state,
    1 AS requester_id,
    'admin' AS requester_name,
    1 AS operator_id,
    'admin' AS operator_name,
    CONCAT('delivery_split_2026 row:', t.row_no) AS remark,
    1 AS approver_id,
    'admin' AS approver_name,
    NULL AS approve_time,
    STR_TO_DATE(CAST(t.closed_at AS CHAR), '%Y-%m-%d') AS biz_date,
    0 AS version,
    NULL AS finish_time,
    NULL AS outbound_mode,
    NULL AS customer_id,
    NULL AS customer_name,
    NULL AS dept_id,
    NULL AS dept_code,
    STR_TO_DATE(CAST(t.closed_at AS CHAR), '%Y-%m-%d') AS sale_deadline,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM delivery_split_test t;

INSERT INTO t_stock_order_item
(
    order_id, goods_id, sku_id, sku_code, goods_name, english_name, brand_id, brand_name,
    series_id, series_name, category_id, category_name, stock_type_id, stock_type_name,
    maker_id, maker_name, before_qty, change_qty, after_qty, price, currency, biz_date,
    remark, deleted, created_by, updated_by, create_time, update_time
)
SELECT
    so.id AS order_id,
    g.id AS goods_id,
    sku.id AS sku_id,
    sku.sku_code AS sku_code,
    g.name AS goods_name,
    g.english_name AS english_name,
    g.brand_id AS brand_id,
    b.name AS brand_name,
    g.series_id AS series_id,
    s.name AS series_name,
    g.category_id AS category_id,
    c.name AS category_name,
    g.category_id AS stock_type_id,
    st.name AS stock_type_name,
    g.maker_id AS maker_id,
    m.name AS maker_name,
    0 AS before_qty,
    COALESCE(t.qty_delivered, 0) AS change_qty,
    COALESCE(t.qty_delivered, 0) AS after_qty,
    COALESCE(t.fob_price, 0) AS price,
    'JPY' AS currency,
    STR_TO_DATE(CAST(t.closed_at AS CHAR), '%Y-%m-%d') AS biz_date,
    CONCAT('delivery_split_2026 row:', t.row_no) AS remark,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM delivery_split_test t
JOIN t_stock_order so
    ON so.source_id = t.row_no
   AND so.remark = CONCAT('delivery_split_2026 row:', t.row_no)
JOIN t_goods_sku sku
    ON sku.sku_code = t.item_code
   AND sku.deleted = 0
JOIN t_goods g
    ON g.id = sku.goods_id
   AND g.deleted = 0
LEFT JOIN t_brand b ON b.id = g.brand_id AND b.deleted = 0
LEFT JOIN t_series s ON s.id = g.series_id AND s.deleted = 0
LEFT JOIN t_category c ON c.id = g.category_id AND c.deleted = 0
LEFT JOIN t_stock_type st ON st.id = g.category_id AND st.deleted = 0
LEFT JOIN t_maker m ON m.id = g.maker_id AND m.deleted = 0;

INSERT INTO t_stock_batch
(
    inbound_order_id, inbound_order_item_id, stock_id, goods_id, sku_id, warehouse_id, stock_type_id,
    original_qty, available_qty, allocated_qty, customer_out_qty, sale_deadline, state, version,
    deleted, created_by, updated_by, create_time, update_time
)
SELECT
    soi.order_id AS inbound_order_id,
    soi.id AS inbound_order_item_id,
    st.id AS stock_id,
    soi.goods_id,
    soi.sku_id,
    so.warehouse_id,
    soi.stock_type_id,
    COALESCE(t.qty_delivered, 0) AS original_qty,
    COALESCE(t.qty_delivered, 0) AS available_qty,
    0 AS allocated_qty,
    0 AS customer_out_qty,
    so.sale_deadline,
    0 AS state,
    0 AS version,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM delivery_split_test t
JOIN t_stock_order so
    ON so.source_id = t.row_no
   AND so.remark = CONCAT('delivery_split_2026 row:', t.row_no)
JOIN t_stock_order_item soi
    ON soi.order_id = so.id
   AND soi.remark = CONCAT('delivery_split_2026 row:', t.row_no)
JOIN t_stock st
    ON st.goods_id = soi.goods_id
   AND st.sku_id = soi.sku_id
   AND st.warehouse_id = so.warehouse_id
   AND st.deleted = 0;

INSERT INTO t_group_stock
(
    batch_id, dept_id, dept_code, stock_id, goods_id, sku_id, warehouse_id, stock_type_id,
    allocated_qty, current_qty, sale_deadline, state, version, deleted, created_by, updated_by,
    create_time, update_time
)
SELECT
    sb.id AS batch_id,
    d.id AS dept_id,
    d.code AS dept_code,
    sb.stock_id,
    sb.goods_id,
    sb.sku_id,
    sb.warehouse_id,
    sb.stock_type_id,
    CASE d.code
        WHEN 'A' THEN COALESCE(t.a_split, 0)
        WHEN 'B' THEN COALESCE(t.b_split, 0)
        WHEN 'C' THEN COALESCE(t.c_split, 0)
        ELSE 0
    END AS allocated_qty,
    CASE d.code
        WHEN 'A' THEN COALESCE(t.a_total, 0)
        WHEN 'B' THEN COALESCE(t.b_total, 0)
        WHEN 'C' THEN COALESCE(t.c_total, 0)
        ELSE 0
    END AS current_qty,
    sb.sale_deadline,
    0 AS state,
    0 AS version,
    0 AS deleted,
    1 AS created_by,
    1 AS updated_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM delivery_split_test t
JOIN t_stock_order so
    ON so.source_id = t.row_no
   AND so.remark = CONCAT('delivery_split_2026 row:', t.row_no)
JOIN t_stock_batch sb
    ON sb.inbound_order_id = so.id
JOIN t_dept d
    ON d.code IN ('A', 'B', 'C')
WHERE
    CASE d.code
        WHEN 'A' THEN COALESCE(t.a_split, 0)
        WHEN 'B' THEN COALESCE(t.b_split, 0)
        WHEN 'C' THEN COALESCE(t.c_split, 0)
        ELSE 0
    END > 0;

SET FOREIGN_KEY_CHECKS = 1;
