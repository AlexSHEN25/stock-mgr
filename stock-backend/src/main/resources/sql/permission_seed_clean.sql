SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM t_role_permission WHERE role_id = 1;
DELETE FROM t_permission WHERE id BETWEEN 1 AND 86;

INSERT INTO `t_permission`
(`id`, `name`, `code`, `module`, `type`, `parent_id`, `path`, `sort`, `icon`, `component`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES
    (1,'システム管理','MENU_SYSTEM','system',1,0,'/system',1,'setting','system/index',1,0,1,1,NOW(),NOW()),
    (2,'ユーザー管理','MENU_USER','user',1,1,'/user',2,'user','user/index',1,0,1,1,NOW(),NOW()),
    (3,'商品管理','MENU_GOODS','goods',1,1,'/goods',3,'goods','goods/index',1,0,1,1,NOW(),NOW()),
    (4,'在庫管理','MENU_STOCK','stock',1,1,'/stock',4,'stock','stock/index',1,0,1,1,NOW(),NOW()),
    (5,'顧客管理','MENU_CUSTOMER','customer',1,1,'/customer',5,'customer','customer/index',1,0,1,1,NOW(),NOW()),

    (6,'在庫管理閲覧','DATA_STOCK_READ','stock',2,4,'/api/stock/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (7,'在庫管理編集','DATA_STOCK_WRITE','stock',2,4,'/api/stock/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (8,'在庫分類閲覧','DATA_STOCK_TYPE_READ','stock',2,4,'/api/stockType/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (9,'在庫分類編集','DATA_STOCK_TYPE_WRITE','stock',2,4,'/api/stockType/**',4,'api','',1,0,1,1,NOW(),NOW()),
    (10,'倉庫管理閲覧','DATA_WAREHOUSE_READ','stock',2,4,'/api/warehouse/**',5,'api','',1,0,1,1,NOW(),NOW()),
    (11,'倉庫管理編集','DATA_WAREHOUSE_WRITE','stock',2,4,'/api/warehouse/**',6,'api','',1,0,1,1,NOW(),NOW()),
    (12,'入出庫伝票閲覧','DATA_STOCK_ORDER_READ','stock',2,4,'/api/stockOrder/**',7,'api','',1,0,1,1,NOW(),NOW()),
    (13,'入出庫伝票編集','DATA_STOCK_ORDER_WRITE','stock',2,4,'/api/stockOrder/**',8,'api','',1,0,1,1,NOW(),NOW()),
    (14,'入出庫明細閲覧','DATA_STOCK_ORDER_ITEM_READ','stock',2,4,'/api/stockOrderItem/**',9,'api','',1,0,1,1,NOW(),NOW()),
    (15,'入出庫明細編集','DATA_STOCK_ORDER_ITEM_WRITE','stock',2,4,'/api/stockOrderItem/**',10,'api','',1,0,1,1,NOW(),NOW()),
    (16,'在庫履歴閲覧','DATA_STOCK_RECORD_READ','stock',2,4,'/api/stockRecord/**',11,'api','',1,0,1,1,NOW(),NOW()),
    (17,'在庫履歴編集','DATA_STOCK_RECORD_WRITE','stock',2,4,'/api/stockRecord/**',12,'api','',1,0,1,1,NOW(),NOW()),
    (18,'価格履歴閲覧','DATA_PRICE_RECORD_READ','stock',2,4,'/api/priceRecord/**',13,'api','',1,0,1,1,NOW(),NOW()),
    (19,'価格履歴編集','DATA_PRICE_RECORD_WRITE','stock',2,4,'/api/priceRecord/**',14,'api','',1,0,1,1,NOW(),NOW()),
    (20,'請求書管理閲覧','DATA_REQUEST_FORM_READ','stock',2,4,'/api/requestForm/**',15,'api','',1,0,1,1,NOW(),NOW()),
    (21,'請求書管理編集','DATA_REQUEST_FORM_WRITE','stock',2,4,'/api/requestForm/**',16,'api','',1,0,1,1,NOW(),NOW()),

    (24,'顧客管理閲覧','DATA_CUSTOMER_READ','customer',2,5,'/api/customer/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (25,'顧客管理編集','DATA_CUSTOMER_WRITE','customer',2,5,'/api/customer/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (26,'顧客ランク管理閲覧','DATA_CUSTOMER_LEVEL_READ','customer',2,5,'/api/customerLevel/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (27,'顧客ランク管理編集','DATA_CUSTOMER_LEVEL_WRITE','customer',2,5,'/api/customerLevel/**',4,'api','',1,0,1,1,NOW(),NOW()),

    (28,'ユーザー管理閲覧','DATA_USER_READ','user',2,2,'/api/user/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (29,'商品管理閲覧','DATA_GOODS_READ','goods',2,3,'/api/goods/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (30,'商品管理閲覧','DATA_GOODS_SKU_READ','goods',2,3,'/api/goodsSku/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (31,'商品管理閲覧','DATA_GOODS_IMAGE_READ','goods',2,3,'/api/goodsImage/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (32,'商品管理閲覧','DATA_GOODS_SKU_SPEC_READ','goods',2,3,'/api/goodsSkuSpec/**',4,'api','',1,0,1,1,NOW(),NOW()),
    (33,'メッセージ管理閲覧','DATA_MESSAGE_READ','system',2,1,'/api/message/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (34,'メッセージ管理編集','DATA_MESSAGE_WRITE','system',2,1,'/api/message/**',2,'api','',1,0,1,1,NOW(),NOW());

DELETE FROM t_role_permission WHERE role_id = 1;
INSERT INTO `t_role_permission`
(`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES
    (1,1,1,0,1,1,NOW(),NOW()),
    (2,1,2,0,1,1,NOW(),NOW()),
    (3,1,3,0,1,1,NOW(),NOW()),
    (4,1,4,0,1,1,NOW(),NOW()),
    (5,1,5,0,1,1,NOW(),NOW()),
    (6,1,6,0,1,1,NOW(),NOW()),
    (7,1,7,0,1,1,NOW(),NOW()),
    (8,1,8,0,1,1,NOW(),NOW()),
    (9,1,9,0,1,1,NOW(),NOW()),
    (10,1,10,0,1,1,NOW(),NOW()),
    (11,1,11,0,1,1,NOW(),NOW()),
    (12,1,12,0,1,1,NOW(),NOW()),
    (13,1,13,0,1,1,NOW(),NOW()),
    (14,1,14,0,1,1,NOW(),NOW()),
    (15,1,15,0,1,1,NOW(),NOW()),
    (16,1,16,0,1,1,NOW(),NOW()),
    (17,1,17,0,1,1,NOW(),NOW()),
    (18,1,18,0,1,1,NOW(),NOW()),
    (19,1,19,0,1,1,NOW(),NOW()),
    (20,1,20,0,1,1,NOW(),NOW()),
    (21,1,21,0,1,1,NOW(),NOW()),
    (22,1,24,0,1,1,NOW(),NOW()),
    (23,1,25,0,1,1,NOW(),NOW()),
    (24,1,26,0,1,1,NOW(),NOW()),
    (25,1,27,0,1,1,NOW(),NOW()),
    (26,1,28,0,1,1,NOW(),NOW()),
    (27,1,29,0,1,1,NOW(),NOW()),
    (28,1,30,0,1,1,NOW(),NOW()),
    (29,1,31,0,1,1,NOW(),NOW()),
    (30,1,32,0,1,1,NOW(),NOW()),
    (31,1,33,0,1,1,NOW(),NOW()),
    (32,1,34,0,1,1,NOW(),NOW());

SET FOREIGN_KEY_CHECKS = 1;
