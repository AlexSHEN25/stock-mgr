SET NAMES utf8mb4;

-- =========================
-- 基础组织与权限
-- =========================
INSERT INTO t_dept (id, parent_id, name, code, leader_id, sort, status, deleted, create_time, update_time) VALUES
(1001, 0, '总部', 'HQ', 2001, 1, 1, 0, NOW(), NOW()),
(1002, 1001, '采购部', 'PURCHASE', 2002, 2, 1, 0, NOW(), NOW()),
(1003, 1001, '仓储部', 'WAREHOUSE', 2003, 3, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=NOW();

INSERT INTO t_user (id, username, password, dept_id, salt, email, phone, avatar, status, deleted, create_time, update_time) VALUES
(2001, 'admin', '6f3f8c4f2ec2f9d8f2abf5f6d3f87123', 1001, 'a1b2c3', 'admin@kitchen.test', '09000000001', NULL, 1, 0, NOW(), NOW()),
(2002, 'buyer01', '6f3f8c4f2ec2f9d8f2abf5f6d3f87123', 1002, 'a1b2c3', 'buyer01@kitchen.test', '09000000002', NULL, 1, 0, NOW(), NOW()),
(2003, 'warehouse01', '6f3f8c4f2ec2f9d8f2abf5f6d3f87123', 1003, 'a1b2c3', 'warehouse01@kitchen.test', '09000000003', NULL, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE dept_id=VALUES(dept_id), email=VALUES(email), phone=VALUES(phone), update_time=NOW();

INSERT INTO t_role (id, name, code, remark, status, deleted, create_time, update_time) VALUES
(3001, '系统管理员', 'ADMIN', '全权限', 1, 0, NOW(), NOW()),
(3002, '采购专员', 'BUYER', '采购与请求书处理', 1, 0, NOW(), NOW()),
(3003, '仓库专员', 'WH_OPERATOR', '库存处理', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), remark=VALUES(remark), update_time=NOW();

INSERT INTO t_permission (id, name, code, module, type, parent_id, path, sort, icon, component, status, deleted, create_time, update_time) VALUES
(4001, '库存管理', 'stock:menu', 'stock', 1, 0, '/stock', 1, 'box', 'StockPage', 1, 0, NOW(), NOW()),
(4002, '库存查询', 'stock:query', 'stock', 3, 4001, '/stock/page', 2, NULL, NULL, 1, 0, NOW(), NOW()),
(4003, '商品管理', 'goods:menu', 'goods', 1, 0, '/goods', 3, 'goods', 'GoodsPage', 1, 0, NOW(), NOW()),
(4004, '请求书管理', 'request:menu', 'request', 1, 0, '/request', 4, 'file', 'RequestPage', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), module=VALUES(module), update_time=NOW();

INSERT INTO t_user_role (id, user_id, role_id, deleted, create_time, update_time) VALUES
(5001, 2001, 3001, 0, NOW(), NOW()),
(5002, 2002, 3002, 0, NOW(), NOW()),
(5003, 2003, 3003, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE deleted=0, update_time=NOW();

INSERT INTO t_role_permission (id, role_id, permission_id, deleted, create_time, update_time) VALUES
(6001, 3001, 4001, 0, NOW(), NOW()),
(6002, 3001, 4002, 0, NOW(), NOW()),
(6003, 3001, 4003, 0, NOW(), NOW()),
(6004, 3001, 4004, 0, NOW(), NOW()),
(6005, 3002, 4003, 0, NOW(), NOW()),
(6006, 3002, 4004, 0, NOW(), NOW()),
(6007, 3003, 4001, 0, NOW(), NOW()),
(6008, 3003, 4002, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE deleted=0, update_time=NOW();

-- =========================
-- 厨房用品主数据
-- =========================
INSERT INTO t_warehouse (id, name, code, address, manager_id, status, deleted, create_time, update_time) VALUES
(7001, '东京中央仓', 'WH-TYO-01', 'Tokyo Minato', 2003, 1, 0, NOW(), NOW()),
(7002, '大阪仓', 'WH-OSA-01', 'Osaka Naniwa', 2003, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), address=VALUES(address), update_time=NOW();

INSERT INTO t_brand (id, name, english_name, image, content, status, deleted, create_time, update_time) VALUES
(8001, '和匠', 'WASHO', NULL, '日式厨房工具品牌', 1, 0, NOW(), NOW()),
(8002, '鋼锋', 'STEEL EDGE', NULL, '不锈钢厨具品牌', 1, 0, NOW(), NOW()),
(8003, '木语', 'WOOD TALK', NULL, '木制厨房用品', 1, 0, NOW(), NOW()),
(8004, '热厨', 'HEATCHEF', NULL, '锅具与炊具', 1, 0, NOW(), NOW()),
(8005, '清居', 'CLEANHOME', NULL, '厨房清洁收纳', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), english_name=VALUES(english_name), update_time=NOW();

INSERT INTO t_goods_type (id, name, status, deleted, create_time, update_time) VALUES
(8101, '刀具', 1, 0, NOW(), NOW()),
(8102, '锅具', 1, 0, NOW(), NOW()),
(8103, '餐厨小件', 1, 0, NOW(), NOW()),
(8104, '收纳', 1, 0, NOW(), NOW()),
(8105, '清洁用品', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=NOW();

INSERT INTO t_series (id, name, english_name, content, status, deleted, create_time, update_time) VALUES
(8201, '锋刃系列', 'Blade Pro', '刀具系列', 1, 0, NOW(), NOW()),
(8202, '热源系列', 'Heat Core', '锅具系列', 1, 0, NOW(), NOW()),
(8203, '烘焙系列', 'Bake Easy', '烘焙与量具', 1, 0, NOW(), NOW()),
(8204, '收纳系列', 'Store Fit', '厨房收纳', 1, 0, NOW(), NOW()),
(8205, '清洁系列', 'Clean Plus', '清洁用品', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), english_name=VALUES(english_name), update_time=NOW();

INSERT INTO t_maker (id, name, status, deleted, create_time, update_time) VALUES
(8301, '东京工厂', 1, 0, NOW(), NOW()),
(8302, '大阪工厂', 1, 0, NOW(), NOW()),
(8303, '名古屋工厂', 1, 0, NOW(), NOW()),
(8304, '福冈工厂', 1, 0, NOW(), NOW()),
(8305, '神户工厂', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=NOW();

INSERT INTO t_goods
(id, name, english_name, sku, series_id, brand_id, type_id, maker_id, price, discount, status, new_price, price_update_time, images, description, is_hot, version, deleted, create_time, update_time) VALUES
(9001, '主厨刀 8寸', 'Chef Knife 8in', 'KIT-KNIFE-8-001', 8201, 8002, 8101, 8301, 128.00, 0.9500, 1, NULL, NULL, NULL, '高碳不锈钢主厨刀', 1, 1, 0, NOW(), NOW()),
(9002, '三德刀 7寸', 'Santoku Knife 7in', 'KIT-KNIFE-7-002', 8201, 8002, 8101, 8301, 118.00, 0.9300, 1, NULL, NULL, NULL, '多功能三德刀', 1, 1, 0, NOW(), NOW()),
(9003, '不粘平底锅 28cm', 'Nonstick Pan 28cm', 'KIT-PAN-28-003', 8202, 8004, 8102, 8302, 189.00, 0.9000, 1, NULL, NULL, NULL, '适合煎炒', 1, 1, 0, NOW(), NOW()),
(9004, '汤锅 24cm', 'Stock Pot 24cm', 'KIT-POT-24-004', 8202, 8004, 8102, 8302, 229.00, 0.9200, 1, NULL, NULL, NULL, '加厚复底汤锅', 0, 1, 0, NOW(), NOW()),
(9005, '硅胶锅铲', 'Silicone Spatula', 'KIT-SPATULA-005', 8203, 8001, 8103, 8303, 39.00, 1.0000, 1, NULL, NULL, NULL, '耐高温硅胶', 0, 1, 0, NOW(), NOW()),
(9006, '量杯套装', 'Measuring Cup Set', 'KIT-MEASURE-006', 8203, 8001, 8103, 8303, 49.00, 0.9800, 1, NULL, NULL, NULL, '四件套量杯', 0, 1, 0, NOW(), NOW()),
(9007, '木砧板 大号', 'Wood Cutting Board L', 'KIT-BOARD-L-007', 8203, 8003, 8103, 8304, 88.00, 0.9700, 1, NULL, NULL, NULL, '实木砧板', 0, 1, 0, NOW(), NOW()),
(9008, '刀具收纳架', 'Knife Holder', 'KIT-HOLDER-008', 8204, 8005, 8104, 8305, 79.00, 0.9500, 1, NULL, NULL, NULL, '台面刀架', 0, 1, 0, NOW(), NOW()),
(9009, '调味罐 6件套', 'Spice Jar Set', 'KIT-SPICE-009', 8204, 8005, 8104, 8305, 69.00, 0.9600, 1, NULL, NULL, NULL, '玻璃调味罐', 0, 1, 0, NOW(), NOW()),
(9010, '洗碗刷 2支装', 'Dish Brush Set', 'KIT-BRUSH-010', 8205, 8005, 8105, 8304, 29.00, 1.0000, 1, NULL, NULL, NULL, '厨房清洁刷', 0, 1, 0, NOW(), NOW()),
(9011, '抹布 5条装', 'Kitchen Cloth 5pcs', 'KIT-CLOTH-011', 8205, 8005, 8105, 8304, 25.00, 1.0000, 1, NULL, NULL, NULL, '高吸水抹布', 0, 1, 0, NOW(), NOW()),
(9012, '保鲜盒 8件套', 'Food Container 8pcs', 'KIT-BOX-012', 8204, 8005, 8104, 8305, 109.00, 0.9400, 1, NULL, NULL, NULL, '厨房收纳保鲜', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), discount=VALUES(discount), update_time=NOW();

INSERT INTO t_stock
(id, goods_id, goods_name, sku, warehouse_id, current_qty, lock_qty, price, price_update_time, status, version, deleted, create_time, update_time) VALUES
(9101, 9001, '主厨刀 8寸', 'KIT-KNIFE-8-001', 7001, 160, 12, 128.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9102, 9002, '三德刀 7寸', 'KIT-KNIFE-7-002', 7001, 160, 8, 118.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9103, 9003, '不粘平底锅 28cm', 'KIT-PAN-28-003', 7001, 110, 10, 189.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9104, 9004, '汤锅 24cm', 'KIT-POT-24-004', 7001, 90, 5, 229.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9105, 9005, '硅胶锅铲', 'KIT-SPATULA-005', 7001, 260, 20, 39.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9106, 9006, '量杯套装', 'KIT-MEASURE-006', 7001, 220, 14, 49.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9107, 9007, '木砧板 大号', 'KIT-BOARD-L-007', 7002, 180, 9, 88.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9108, 9008, '刀具收纳架', 'KIT-HOLDER-008', 7002, 130, 11, 79.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9109, 9009, '调味罐 6件套', 'KIT-SPICE-009', 7002, 150, 12, 69.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9110, 9010, '洗碗刷 2支装', 'KIT-BRUSH-010', 7002, 360, 30, 29.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9111, 9011, '抹布 5条装', 'KIT-CLOTH-011', 7002, 340, 35, 25.00, NOW(), 1, 1, 0, NOW(), NOW()),
(9112, 9012, '保鲜盒 8件套', 'KIT-BOX-012', 7001, 140, 13, 109.00, NOW(), 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE current_qty=VALUES(current_qty), lock_qty=VALUES(lock_qty), price=VALUES(price), update_time=NOW();

INSERT INTO t_price_record
(id, goods_id, goods_name, english_name, sku, old_price, new_price, discount, price_update_time, operator_id, operator_name, deleted, create_time, update_time) VALUES
(9201, 9001, '主厨刀 8寸', 'Chef Knife 8in', 'KIT-KNIFE-8-001', 138.00, 128.00, 0.9500, NOW(), 2001, 'admin', 0, NOW(), NOW()),
(9202, 9003, '不粘平底锅 28cm', 'Nonstick Pan 28cm', 'KIT-PAN-28-003', 199.00, 189.00, 0.9000, NOW(), 2001, 'admin', 0, NOW(), NOW()),
(9203, 9012, '保鲜盒 8件套', 'Food Container 8pcs', 'KIT-BOX-012', 119.00, 109.00, 0.9400, NOW(), 2002, 'buyer01', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE new_price=VALUES(new_price), discount=VALUES(discount), update_time=NOW();

-- =========================
-- 客户与请求/库存单据
-- =========================
INSERT INTO t_customer_level (id, name, discount, remark, status, deleted, create_time, update_time) VALUES
(1, '普通客户', 1.0000, '默认等级', 1, 0, NOW(), NOW()),
(2, '银牌客户', 0.9500, '月采购较高', 1, 0, NOW(), NOW()),
(3, '金牌客户', 0.9000, '重点客户', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), discount=VALUES(discount), update_time=NOW();

INSERT INTO t_customer
(id, customer_code, name, english_name, contact_person, phone, email, country, city, address, level_id, remark, status, deleted, create_time, update_time) VALUES
(9401, 'CUS-KT-001', '东京餐饮株式会社', 'Tokyo Dining Co.,Ltd', 'Sato', '0311110001', 'tokyo-dining@test.co', 'Japan', 'Tokyo', 'Minato 1-2-3', 3, '重点客户', 1, 0, NOW(), NOW()),
(9402, 'CUS-KT-002', '大阪厨房用品店', 'Osaka Kitchen Store', 'Tanaka', '0611110002', 'osaka-store@test.co', 'Japan', 'Osaka', 'Naniwa 2-3-4', 2, NULL, 1, 0, NOW(), NOW()),
(9403, 'CUS-KT-003', '名古屋烘焙坊', 'Nagoya Bakery', 'Suzuki', '0521110003', 'nagoya-bakery@test.co', 'Japan', 'Nagoya', 'Naka 3-4-5', 1, NULL, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), level_id=VALUES(level_id), update_time=NOW();

INSERT INTO t_request_form
(id, biz_no, user_id, username, dept_id, dept_name, customer_id, customer_name, warehouse_id, total_qty, request_qty, state, approver_id, approve_name, approve_time, approve_remark, deleted, create_time, update_time) VALUES
(9501, 'REQ20260327001', 2002, 'buyer01', 1002, '采购部', 9401, '东京餐饮株式会社', 7001, 80, 80, 2, 2001, 'admin', NOW(), '同意出库', 0, NOW(), NOW()),
(9502, 'REQ20260327002', 2002, 'buyer01', 1002, '采购部', 9402, '大阪厨房用品店', 7002, 55, 55, 1, NULL, NULL, NULL, NULL, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE state=VALUES(state), total_qty=VALUES(total_qty), request_qty=VALUES(request_qty), update_time=NOW();

INSERT INTO t_stock_order
(id, order_no, type, warehouse_id, source_type, source_id, total_qty, state, requester_id, requester_name, operator_id, operator_name, remark, approver_id, approver_name, approve_time, finish_time, deleted, create_time, update_time) VALUES
(9601, 'SO20260327001', 2, 7001, 3, 9501, 80, 2, 2002, 'buyer01', 2003, 'warehouse01', '根据请求书出库', 2001, 'admin', NOW(), NOW(), 0, NOW(), NOW()),
(9602, 'SO20260327002', 1, 7002, 4, NULL, 120, 2, 2003, 'warehouse01', 2003, 'warehouse01', '补货入库', 2001, 'admin', NOW(), NOW(), 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE total_qty=VALUES(total_qty), state=VALUES(state), update_time=NOW();

INSERT INTO t_stock_order_item
(id, order_id, goods_id, sku, goods_name, english_name, brand_id, brand_name, series_id, series_name, type_id, type_name, maker_id, maker_name, before_qty, change_qty, after_qty, price, remark, deleted, create_time, update_time) VALUES
(9701, 9601, 9001, 'KIT-KNIFE-8-001', '主厨刀 8寸', 'Chef Knife 8in', 8002, '鋼锋', 8201, '锋刃系列', 8101, '刀具', 8301, '东京工厂', 180, -20, 160, 128.00, '客户出库', 0, NOW(), NOW()),
(9702, 9601, 9003, 'KIT-PAN-28-003', '不粘平底锅 28cm', 'Nonstick Pan 28cm', 8004, '热厨', 8202, '热源系列', 8102, '锅具', 8302, '大阪工厂', 140, -30, 110, 189.00, '客户出库', 0, NOW(), NOW()),
(9703, 9601, 9012, 'KIT-BOX-012', '保鲜盒 8件套', 'Food Container 8pcs', 8005, '清居', 8204, '收纳系列', 8104, '收纳', 8305, '神户工厂', 170, -30, 140, 109.00, '客户出库', 0, NOW(), NOW()),
(9704, 9602, 9007, 'KIT-BOARD-L-007', '木砧板 大号', 'Wood Cutting Board L', 8003, '木语', 8203, '烘焙系列', 8103, '餐厨小件', 8304, '福冈工厂', 120, 60, 180, 88.00, '补货入库', 0, NOW(), NOW()),
(9705, 9602, 9010, 'KIT-BRUSH-010', '洗碗刷 2支装', 'Dish Brush Set', 8005, '清居', 8205, '清洁系列', 8105, '清洁用品', 8304, '福冈工厂', 300, 60, 360, 29.00, '补货入库', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE after_qty=VALUES(after_qty), change_qty=VALUES(change_qty), update_time=NOW();

INSERT INTO t_stock_record
(id, biz_no, order_id, order_item_id, stock_id, goods_id, sku, goods_name, english_name, brand_id, brand_name, series_id, series_name, type_id, type_name, maker_id, maker_name, warehouse_id, before_qty, change_qty, after_qty, type, source_type, price, price_update_time, customer_id, customer_name, requester_id, requester_name, operator_id, operator_name, remark, deleted, create_time, update_time) VALUES
(9801, 'BIZ20260327001', 9601, 9701, 9101, 9001, 'KIT-KNIFE-8-001', '主厨刀 8寸', 'Chef Knife 8in', 8002, '鋼锋', 8201, '锋刃系列', 8101, '刀具', 8301, '东京工厂', 7001, 180, -20, 160, 2, 3, 128.00, NOW(), 9401, '东京餐饮株式会社', 2002, 'buyer01', 2003, 'warehouse01', '请求单出库', 0, NOW(), NOW()),
(9802, 'BIZ20260327002', 9601, 9702, 9103, 9003, 'KIT-PAN-28-003', '不粘平底锅 28cm', 'Nonstick Pan 28cm', 8004, '热厨', 8202, '热源系列', 8102, '锅具', 8302, '大阪工厂', 7001, 140, -30, 110, 2, 3, 189.00, NOW(), 9401, '东京餐饮株式会社', 2002, 'buyer01', 2003, 'warehouse01', '请求单出库', 0, NOW(), NOW()),
(9803, 'BIZ20260327003', 9601, 9703, 9112, 9012, 'KIT-BOX-012', '保鲜盒 8件套', 'Food Container 8pcs', 8005, '清居', 8204, '收纳系列', 8104, '收纳', 8305, '神户工厂', 7001, 170, -30, 140, 2, 3, 109.00, NOW(), 9401, '东京餐饮株式会社', 2002, 'buyer01', 2003, 'warehouse01', '请求单出库', 0, NOW(), NOW()),
(9804, 'BIZ20260327004', 9602, 9704, 9107, 9007, 'KIT-BOARD-L-007', '木砧板 大号', 'Wood Cutting Board L', 8003, '木语', 8203, '烘焙系列', 8103, '餐厨小件', 8304, '福冈工厂', 7002, 120, 60, 180, 1, 4, 88.00, NOW(), NULL, NULL, 2003, 'warehouse01', 2003, 'warehouse01', '手动入库', 0, NOW(), NOW()),
(9805, 'BIZ20260327005', 9602, 9705, 9110, 9010, 'KIT-BRUSH-010', '洗碗刷 2支装', 'Dish Brush Set', 8005, '清居', 8205, '清洁系列', 8105, '清洁用品', 8304, '福冈工厂', 7002, 300, 60, 360, 1, 4, 29.00, NOW(), NULL, NULL, 2003, 'warehouse01', 2003, 'warehouse01', '手动入库', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE after_qty=VALUES(after_qty), change_qty=VALUES(change_qty), update_time=NOW();

INSERT INTO t_request_item
(id, request_id, goods_id, sku, goods_name, english_name, brand_id, brand_name, series_id, series_name, type_id, type_name, maker_id, maker_name, warehouse_id, price, discount, request_qty, approve_qty, out_qty, stock_record_id, remark, deleted, create_time, update_time) VALUES
(9901, 9501, 9001, 'KIT-KNIFE-8-001', '主厨刀 8寸', 'Chef Knife 8in', 8002, '鋼锋', 8201, '锋刃系列', 8101, '刀具', 8301, '东京工厂', 7001, 128.00, 0.95, 20, 20, 20, 9801, '正常出库', 0, NOW(), NOW()),
(9902, 9501, 9003, 'KIT-PAN-28-003', '不粘平底锅 28cm', 'Nonstick Pan 28cm', 8004, '热厨', 8202, '热源系列', 8102, '锅具', 8302, '大阪工厂', 7001, 189.00, 0.90, 30, 30, 30, 9802, '正常出库', 0, NOW(), NOW()),
(9903, 9501, 9012, 'KIT-BOX-012', '保鲜盒 8件套', 'Food Container 8pcs', 8005, '清居', 8204, '收纳系列', 8104, '收纳', 8305, '神户工厂', 7001, 109.00, 0.94, 30, 30, 30, 9803, '正常出库', 0, NOW(), NOW()),
(9904, 9502, 9007, 'KIT-BOARD-L-007', '木砧板 大号', 'Wood Cutting Board L', 8003, '木语', 8203, '烘焙系列', 8103, '餐厨小件', 8304, '福冈工厂', 7002, 88.00, 0.97, 25, 0, 0, NULL, '待审核', 0, NOW(), NOW()),
(9905, 9502, 9009, 'KIT-SPICE-009', '调味罐 6件套', 'Spice Jar Set', 8005, '清居', 8204, '收纳系列', 8104, '收纳', 8305, '神户工厂', 7002, 69.00, 0.96, 30, 0, 0, NULL, '待审核', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE request_qty=VALUES(request_qty), approve_qty=VALUES(approve_qty), out_qty=VALUES(out_qty), update_time=NOW();

-- =========================
-- 其他系统数据
-- =========================
INSERT INTO t_message (id, type, user_id, message, source_id, is_read, state, deleted, create_time, update_time) VALUES
(10001, 1, 2001, '新品上架：主厨刀 8寸', 9001, 0, 1, 0, NOW(), NOW()),
(10002, 1, 2002, '新品上架：不粘平底锅 28cm', 9003, 0, 1, 0, NOW(), NOW()),
(10003, 2, 2003, '库存预警：汤锅 24cm', 9004, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE message=VALUES(message), state=VALUES(state), update_time=NOW();

INSERT INTO t_config (id, name, `group`, title, tip, type, value, content, deleted, create_time, update_time) VALUES
(10101, 'stock.low.threshold', 'stock', '库存预警阈值', '低于该值触发预警', 'int', '50', NULL, 0, NOW(), NOW()),
(10102, 'order.auto.approve', 'order', '自动审核开关', '是否自动审核库存单', 'bool', 'false', NULL, 0, NOW(), NOW()),
(10103, 'goods.default.discount', 'goods', '默认折扣', '新商品默认折扣', 'string', '1.0000', NULL, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE value=VALUES(value), title=VALUES(title), update_time=NOW();

INSERT INTO t_operate_log (id, user_id, username, module, operation, method, request_url, request_ip, request_param, response_data, status, error_msg, cost_time, deleted, create_time, update_time) VALUES
(10201, 2001, 'admin', 'goods', 'IMPORT_TEST_DATA', 'POST', '/goods/import', '127.0.0.1', '{"batch":"kitchen"}', '{"ok":true}', 1, NULL, 45, 0, NOW(), NOW()),
(10202, 2002, 'buyer01', 'request', 'CREATE_REQUEST', 'POST', '/requestForm', '127.0.0.1', '{"bizNo":"REQ20260327001"}', '{"ok":true}', 1, NULL, 31, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE operation=VALUES(operation), status=VALUES(status), update_time=NOW();

INSERT INTO t_user_token (id, token, user_id, login_time, expire_time, login_ip, status, deleted, create_time, update_time) VALUES
(10301, 'TEST_TOKEN_ADMIN_20260327', 2001, NOW(), DATE_ADD(NOW(), INTERVAL 60 MINUTE), '127.0.0.1', 1, 0, NOW(), NOW()),
(10302, 'TEST_TOKEN_BUYER_20260327', 2002, NOW(), DATE_ADD(NOW(), INTERVAL 60 MINUTE), '127.0.0.1', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE expire_time=VALUES(expire_time), status=VALUES(status), update_time=NOW();
