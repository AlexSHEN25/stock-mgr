DROP TABLE IF EXISTS `t_user`;
create TABLE `t_user`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `username`    VARCHAR(64)      NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255)     NOT NULL COMMENT '密码',
    `dept_id`     BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '部门ID',
    `salt`        VARCHAR(32)               DEFAULT NULL COMMENT '密码盐',
    `email`       VARCHAR(128)              DEFAULT NULL COMMENT '电子邮箱',
    `phone`       VARCHAR(32)               DEFAULT NULL COMMENT '联系方式',
    `avatar`      VARCHAR(255)              DEFAULT NULL COMMENT '头像',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_username (username, deleted)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT ='用户表';

INSERT INTO `t_user`
(`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES (1, 'admin', '53ed35e1ac326eec04db3a65aa7f0276', 1, 'salt_admin', 'admin@test.com', '09000000001',
        '/avatar/upload/1_0cf14549-8988-4f0d-9132-975344b88071.png', 1, 0, 1, 1, '2026-05-25 15:42:05',
        '2026-05-25 15:42:05'),
       (2, 'sales01', '285cb306e6caa304a5cdbf9d2f9bfacb', 2, 'salt_sales01', 'sales01@test.com', '09000000002',
        '/avatar/sales01.png', 1, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (3, 'warehouse01', '8750063d034114ef8d4aca57898550dd', 3, 'salt_wh01', 'warehouse01@test.com',
        '09000000003', '/avatar/warehouse01.png', 1, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (4, 'viewer01', '4fbc57eea3b37fdc30dc096dbc483176', 1, 'salt_viewer', 'viewer01@test.com', '09000000004',
        '/avatar/viewer01.png', 1, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (5, 'sales02', '256c07013bc6ec48d0d3483ad34592bf', 2, '60e92491f5099c82bfe87aaf4a5247c6', 'sales02@handk.o',
        '07711112222', '/avatar/upload/mpq9wepm05c389.png', 1, 0, 1, 1, '2026-05-27 21:10:26',
        '2026-05-29 10:58:38');

DROP TABLE IF EXISTS `t_dept`;
CREATE TABLE `t_dept`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '父部门ID',
    `name`        VARCHAR(100)     NOT NULL COMMENT '部门名称',
    `code`        VARCHAR(64)               DEFAULT NULL COMMENT '部门编码',
    `leader_id`   INT UNSIGNED              DEFAULT NULL COMMENT '部门负责人ID',
    `sort`        INT              NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1正常0停用',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`code`, deleted),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='部门表';

INSERT INTO `t_dept`
(`id`, `parent_id`, `name`, `code`, `leader_id`, `sort`, `status`, `deleted`, `created_by`, `updated_by`)
VALUES (1, 0, 'Aグループ', 'A', NULL, 1, 1, 0, 1, 1),
       (2, 0, 'Bグループ', 'B', NULL, 2, 1, 0, 1, 1),
       (3, 0, 'Cグループ', 'C', NULL, 3, 1, 0, 1, 1);


DROP TABLE IF EXISTS `t_user_token`;
create TABLE `t_user_token`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `token`       varchar(128)     NOT NULL COMMENT 'Token',
    `user_id`     BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `login_time`  DATETIME                  DEFAULT NULL COMMENT '登录时间',
    `expire_time` DATETIME                  DEFAULT NULL COMMENT 'token过期时间',
    `login_ip`    varchar(50)               DEFAULT NULL COMMENT '登录IP',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`, deleted),
    KEY idx_user_id (user_id),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='用户登录状态表';

DROP TABLE IF EXISTS `t_role`;
create TABLE `t_role`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name`        VARCHAR(64)      NOT NULL COMMENT '角色名称',
    `code`        VARCHAR(64)      NOT NULL COMMENT '角色编码',
    `remark`      VARCHAR(255)              DEFAULT NULL COMMENT '备注',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_role_code (code, deleted)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='角色表';

INSERT INTO `t_role`
(`id`, `name`, `code`, `remark`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES (1, 'システム管理者', 'ROLE_SUPER_ADMIN', 'システムのデフォルト管理者', 1, 0, 1, 1, '2026-05-25 15:42:05',
        '2026-05-25 15:42:05'),
       (2, '一般ユーザー', 'ROLE_NORMAL_USER', 'システムのデフォルト一般ユーザー', 1, 0, 1, 1, '2026-05-25 15:42:05',
        '2026-05-25 15:42:05');

DROP TABLE IF EXISTS `t_permission`;
create TABLE `t_permission`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `name`        VARCHAR(100)     NOT NULL COMMENT '权限名称',
    `code`        VARCHAR(100)     NOT NULL COMMENT '权限标识',
    `module`      VARCHAR(100)              DEFAULT NULL COMMENT '所属模块',
    `type`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型:1菜单2按钮3接口',
    `parent_id`   BIGINT UNSIGNED           DEFAULT 0 COMMENT '父级权限',
    `path`        VARCHAR(255)              DEFAULT NULL COMMENT '前端路由',
    `sort`        INT              NOT NULL DEFAULT 0 COMMENT '排序',
    `icon`        VARCHAR(100)              DEFAULT NULL COMMENT '图标',
    `component`   VARCHAR(255)              DEFAULT NULL COMMENT '前端组件路径',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_permission_code (code, deleted)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='权限表';

DROP TABLE IF EXISTS `t_user_role`;
create TABLE `t_user_role`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT UNSIGNED  NOT NULL COMMENT '角色ID',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='用户角色关系表';

DROP TABLE IF EXISTS `t_role_permission`;
create TABLE `t_role_permission`
(
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT UNSIGNED  NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT UNSIGNED  NOT NULL COMMENT '权限ID',
    `deleted`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='角色权限关系表';

INSERT INTO `t_permission`
(`id`, `name`, `code`, `module`, `type`, `parent_id`, `path`, `sort`, `icon`, `component`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES
    (1,'システム管理','MENU_SYSTEM','system',1,0,'/system',1,'setting','system/index',1,0,1,1,NOW(),NOW()),
    (2,'ユーザー管理','MENU_USER','user',1,1,'/user',2,'user','user/index',1,0,1,1,NOW(),NOW()),
    (3,'商品管理','MENU_GOODS','goods',1,1,'/goods',3,'goods','goods/index',1,0,1,1,NOW(),NOW()),
    (4,'在庫管理','MENU_STOCK','stock',1,1,'/stock',4,'stock','stock/index',1,0,1,1,NOW(),NOW()),
    (5,'顧客管理','MENU_CUSTOMER','customer',1,1,'/customer',5,'customer','customer/index',1,0,1,1,NOW(),NOW()),

    (6,'在庫一覧閲覧','DATA_STOCK_READ','stock',2,4,'/api/stock/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (7,'在庫一覧編集','DATA_STOCK_WRITE','stock',2,4,'/api/stock/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (8,'在庫区分閲覧','DATA_STOCK_TYPE_READ','stock',2,4,'/api/stockType/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (9,'在庫区分編集','DATA_STOCK_TYPE_WRITE','stock',2,4,'/api/stockType/**',4,'api','',1,0,1,1,NOW(),NOW()),
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
    (20,'まとめ納品書閲覧','DATA_REQUEST_FORM_READ','stock',2,4,'/api/requestForm/**',15,'api','',1,0,1,1,NOW(),NOW()),
    (21,'まとめ納品書編集','DATA_REQUEST_FORM_WRITE','stock',2,4,'/api/requestForm/**',16,'api','',1,0,1,1,NOW(),NOW()),

    (24,'顧客管理閲覧','DATA_CUSTOMER_READ','customer',2,5,'/api/customer/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (25,'顧客管理編集','DATA_CUSTOMER_WRITE','customer',2,5,'/api/customer/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (26,'顧客ランク管理閲覧','DATA_CUSTOMER_LEVEL_READ','customer',2,5,'/api/customerLevel/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (27,'顧客ランク管理編集','DATA_CUSTOMER_LEVEL_WRITE','customer',2,5,'/api/customerLevel/**',4,'api','',1,0,1,1,NOW(),NOW()),

    (28,'ユーザー管理閲覧','DATA_USER_READ','user',2,2,'/api/user/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (29,'商品管理閲覧','DATA_GOODS_READ','goods',2,3,'/api/goods/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (30,'商品SKU閲覧','DATA_GOODS_SKU_READ','goods',2,3,'/api/goodsSku/**',2,'api','',1,0,1,1,NOW(),NOW()),
    (31,'商品画像閲覧','DATA_GOODS_IMAGE_READ','goods',2,3,'/api/goodsImage/**',3,'api','',1,0,1,1,NOW(),NOW()),
    (32,'商品SKU仕様閲覧','DATA_GOODS_SKU_SPEC_READ','goods',2,3,'/api/goodsSkuSpec/**',4,'api','',1,0,1,1,NOW(),NOW()),
    (33,'メッセージ管理閲覧','DATA_MESSAGE_READ','system',2,1,'/api/message/**',1,'api','',1,0,1,1,NOW(),NOW()),
    (34,'メッセージ管理編集','DATA_MESSAGE_WRITE','system',2,1,'/api/message/**',2,'api','',1,0,1,1,NOW(),NOW());

INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES
    (1,1,1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (2,1,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (3,1,3,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (4,1,4,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (5,1,5,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (6,1,6,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (7,1,7,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (8,1,8,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (9,1,9,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (10,1,10,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (11,1,11,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (12,1,12,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (13,1,13,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (14,1,14,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (15,1,15,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (16,1,16,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (17,1,17,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (18,1,18,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (19,1,19,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (20,1,20,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (21,1,21,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (22,1,24,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (23,1,25,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (24,1,26,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (25,1,27,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (28,2,4,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (29,2,5,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (30,2,6,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (31,2,8,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (32,2,10,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (33,2,12,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (34,2,14,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (35,2,16,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (36,2,18,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (37,2,20,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (38,2,22,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (39,2,24,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (40,2,26,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (41,2,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (42,2,3,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (43,2,7,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (44,2,9,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (45,2,11,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (46,2,13,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (47,2,15,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (48,2,17,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (49,2,19,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (50,2,21,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (51,2,23,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (52,2,25,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (53,2,27,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (54,2,28,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (55,2,29,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (56,2,30,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (57,2,31,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (58,2,32,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (59,2,33,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05'),
    (60,2,34,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');

INSERT INTO `t_user_role`
(`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`)
VALUES (1, 1, 1, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (2, 2, 2, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (3, 3, 2, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (4, 4, 2, 0, 1, 1, '2026-05-25 15:42:05', '2026-05-25 15:42:05'),
       (5, 5, 1, 0, 1, 1, '2026-05-27 21:10:26', '2026-05-27 21:10:26');

DROP TABLE IF EXISTS `t_stock`;
create TABLE `t_stock`
(
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_id`          BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `goods_name`        VARCHAR(1024)    NOT NULL COMMENT '商品名称',
    `sku_id`            BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`          VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `warehouse_id`      BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '仓库ID',
    `current_qty`       INT(10)          NOT NULL DEFAULT 0 COMMENT '实际库存数量',
    `lock_qty`          INT(10)          NOT NULL DEFAULT 0 COMMENT '已被锁定库存数量',
    `price`             DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '单价',
    `currency`          VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `price_update_time` DATETIME                  DEFAULT NULL COMMENT '价格最后更新时间',
    `stock_type_id`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存商品分类ID',
    `status`            TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `version`           BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '版本控制',
    `deleted`           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_stock (sku_id, warehouse_id, stock_type_id),
    KEY idx_goods (goods_id),
    KEY idx_version (version),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '商品库存表';

DROP TABLE IF EXISTS `t_stock_order`;
CREATE TABLE t_stock_order
(
    `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`       VARCHAR(64)      NOT NULL COMMENT '库存单号',
    `order_type`     TINYINT UNSIGNED NOT NULL COMMENT '单据类型:1 入库 2 出库 3 调整 4 盘点 5 调拨 6 退货',
    `warehouse_id`   BIGINT UNSIGNED  NOT NULL COMMENT '仓库ID',
    `source_type`    TINYINT UNSIGNED NOT NULL COMMENT '来源类型:1订单2退货3请求单4手动',
    `source_id`      BIGINT                    DEFAULT NULL COMMENT '来源ID',
    `total_qty`      INT(10)          NOT NULL DEFAULT 0 COMMENT '总数量',
    `stock_type_id`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存商品分类ID',
    `state`          TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '单据状态:0草稿1审核中2完成3取消',
    `requester_id`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '申请人ID',
    `requester_name` VARCHAR(64)               DEFAULT NULL COMMENT '申请人',
    `operator_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(64)               DEFAULT NULL COMMENT '操作人',
    `remark`         VARCHAR(255)              DEFAULT NULL COMMENT '备注',
    `approver_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '审核人ID',
    `approver_name`  VARCHAR(64)               DEFAULT NULL COMMENT '审核人',
    `approve_time`   DATETIME                  DEFAULT NULL COMMENT '审核时间',
    `biz_date`       DATETIME                  DEFAULT NULL COMMENT '业务日期',
    `version`        BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '版本控制',
    `finish_time`    DATETIME                  DEFAULT NULL COMMENT '完成时间',
    `outbound_mode`  VARCHAR(32)               DEFAULT NULL COMMENT 'CUSTOMER/GROUP_ALLOCATE/GROUP_CUSTOMER',
    `customer_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '客户ID',
    `customer_name`  VARCHAR(255)              DEFAULT NULL COMMENT '客户名称',
    `dept_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '组别部门ID',
    `dept_code`      VARCHAR(32)               DEFAULT NULL COMMENT '组别编码',
    `sale_deadline`  DATETIME                  DEFAULT NULL COMMENT '销售期限',
    `deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_state_deleted (state, deleted),
    KEY idx_wh_state (warehouse_id, state),
    KEY idx_source (source_type, source_id),
    KEY idx_version (version),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT ='库存业务单';

DROP TABLE IF EXISTS `t_stock_order_item`;
CREATE TABLE t_stock_order_item
(
    `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`        BIGINT UNSIGNED  NOT NULL COMMENT '库存单ID',
    `goods_id`        BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_id`          BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`        VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `goods_name`      VARCHAR(255)     NOT NULL COMMENT '商品名称',
    `english_name`    VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',

    `brand_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '品牌ID',
    `brand_name`      VARCHAR(255)              DEFAULT NULL COMMENT '品牌名称',
    `series_id`       BIGINT UNSIGNED           DEFAULT NULL COMMENT '系列ID',
    `series_name`     VARCHAR(255)              DEFAULT NULL COMMENT '系列名称',
    `category_id`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '商品类型ID',
    `category_name`   VARCHAR(255)              DEFAULT NULL COMMENT '商品类型名称',
    `stock_type_id`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存商品分类ID',
    `stock_type_name` VARCHAR(255)              DEFAULT NULL COMMENT '库存商品分类名称',
    `maker_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '厂家ID',
    `maker_name`      VARCHAR(255)              DEFAULT NULL COMMENT '厂家名称',

    `before_qty`      INT(10)          NOT NULL COMMENT '变更前库存',
    `change_qty`      INT(10)          NOT NULL COMMENT '变化数量',
    `after_qty`       INT(10)          NOT NULL COMMENT '变更后库存',
    `price`           DECIMAL(18, 2)            DEFAULT 0 COMMENT '单价',
    `currency`        VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `biz_date`        DATETIME                  DEFAULT NULL COMMENT '业务日期',
    `remark`          VARCHAR(255)              DEFAULT NULL,
    `deleted`         TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `created_by`      BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`      BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_goods` (`goods_id`),
    KEY idx_sku_id (sku_id),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_series_id` (`series_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_maker_id` (`maker_id`),
    KEY idx_stock_type_id (stock_type_id),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='库存单明细';

DROP TABLE IF EXISTS `t_stock_record`;
create TABLE `t_stock_record`
(
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `biz_no`            VARCHAR(64)      NOT NULL COMMENT '业务单号',
    `order_id`          BIGINT UNSIGNED  NOT NULL COMMENT '库存业务单ID',
    `order_item_id`     BIGINT UNSIGNED  NOT NULL COMMENT '库存单明细ID',
    `stock_id`          BIGINT UNSIGNED  NOT NULL COMMENT '库存表ID',
    `goods_id`          BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_id`            BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`          VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `goods_name`        VARCHAR(255)     NOT NULL COMMENT '商品名称',
    `english_name`      VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',

    `brand_id`          BIGINT UNSIGNED           DEFAULT NULL COMMENT '品牌ID',
    `brand_name`        VARCHAR(255)              DEFAULT NULL COMMENT '品牌名称',
    `series_id`         BIGINT UNSIGNED           DEFAULT NULL COMMENT '系列ID',
    `series_name`       VARCHAR(255)              DEFAULT NULL COMMENT '系列名称',

    `category_id`       BIGINT UNSIGNED           DEFAULT NULL COMMENT '商品类型ID',
    `category_name`     VARCHAR(255)              DEFAULT NULL COMMENT '商品类型名称',
    `stock_type_id`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存商品分类ID',
    `stock_type_name`   VARCHAR(255)              DEFAULT NULL COMMENT '库存商品分类名称',
    `maker_id`          BIGINT UNSIGNED           DEFAULT NULL COMMENT '厂家ID',
    `maker_name`        VARCHAR(255)              DEFAULT NULL COMMENT '厂家名称',

    `warehouse_id`      BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '仓库ID',
    `before_qty`        INT(10)          NOT NULL COMMENT '变更前库存',
    `change_qty`        INT(10)          NOT NULL COMMENT '变化数量',
    `after_qty`         INT(10)          NOT NULL COMMENT '变更后库存',
    `order_type`        TINYINT UNSIGNED NOT NULL COMMENT '单据类型:1 入库 2 出库 3 调整 4 盘点 5 调拨 6 退货',
    `source_type`       TINYINT UNSIGNED NOT NULL COMMENT '来源类型:1订单2退货3请求单4手动',
    `price`             DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '单价',
    `currency`          VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `price_update_time` DATETIME                  DEFAULT NULL COMMENT '价格最后更新时间',
    `customer_id`       BIGINT UNSIGNED           DEFAULT NULL COMMENT '客户ID',
    `customer_name`     VARCHAR(255)              DEFAULT NULL COMMENT '客户名称',
    `batch_id`          BIGINT UNSIGNED           DEFAULT NULL COMMENT '入库批次ID',
    `dept_id`           BIGINT UNSIGNED           DEFAULT NULL COMMENT '组别部门ID',
    `dept_code`         VARCHAR(32)               DEFAULT NULL COMMENT '组别编码',
    `outbound_mode`     VARCHAR(32)               DEFAULT NULL COMMENT '出库区分',
    `requester_id`      BIGINT UNSIGNED           DEFAULT NULL COMMENT '申请人id',
    `requester_name`    VARCHAR(64)               DEFAULT NULL COMMENT '申请人名',
    `operator_id`       BIGINT UNSIGNED           DEFAULT NULL COMMENT '操作人id',
    `operator_name`     VARCHAR(64)               DEFAULT NULL COMMENT '操作人名',
    `biz_date`          DATETIME                  DEFAULT NULL COMMENT '业务日期',
    `remark`            VARCHAR(255)              DEFAULT NULL COMMENT '备注',
    `deleted`           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY idx_biz (biz_no),
    KEY idx_goods (goods_id),
    KEY idx_sku (sku_id),
    KEY idx_order_type (order_type),
    KEY idx_customer (customer_id),
    KEY idx_operator (operator_id),
    KEY idx_create_time (create_time),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_series_id` (`series_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_stock_type_id` (`stock_type_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '库存流水表';

DROP TABLE IF EXISTS `t_group_stock`;
DROP TABLE IF EXISTS `t_stock_batch`;
CREATE TABLE `t_stock_batch`
(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `inbound_order_id` BIGINT UNSIGNED NOT NULL,
    `inbound_order_item_id` BIGINT UNSIGNED NOT NULL,
    `stock_id` BIGINT UNSIGNED NOT NULL,
    `goods_id` BIGINT UNSIGNED NOT NULL,
    `sku_id` BIGINT UNSIGNED NOT NULL,
    `warehouse_id` BIGINT UNSIGNED NOT NULL,
    `stock_type_id` BIGINT UNSIGNED DEFAULT NULL,
    `original_qty` INT NOT NULL,
    `available_qty` INT NOT NULL,
    `allocated_qty` INT NOT NULL DEFAULT 0,
    `customer_out_qty` INT NOT NULL DEFAULT 0,
    `sale_deadline` DATETIME DEFAULT NULL,
    `state` TINYINT NOT NULL DEFAULT 0,
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `created_by` BIGINT UNSIGNED DEFAULT NULL,
    `updated_by` BIGINT UNSIGNED DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inbound_item` (`inbound_order_item_id`, `deleted`),
    KEY `idx_batch_stock_deadline` (`stock_id`, `sale_deadline`, `state`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库批次';

CREATE TABLE `t_group_stock`
(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `batch_id` BIGINT UNSIGNED NOT NULL,
    `dept_id` BIGINT UNSIGNED NOT NULL,
    `dept_code` VARCHAR(32) NOT NULL,
    `stock_id` BIGINT UNSIGNED NOT NULL,
    `goods_id` BIGINT UNSIGNED NOT NULL,
    `sku_id` BIGINT UNSIGNED NOT NULL,
    `warehouse_id` BIGINT UNSIGNED NOT NULL,
    `stock_type_id` BIGINT UNSIGNED DEFAULT NULL,
    `allocated_qty` INT NOT NULL,
    `current_qty` INT NOT NULL,
    `sale_deadline` DATETIME DEFAULT NULL,
    `state` TINYINT NOT NULL DEFAULT 0,
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `created_by` BIGINT UNSIGNED DEFAULT NULL,
    `updated_by` BIGINT UNSIGNED DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_dept` (`batch_id`, `dept_id`, `deleted`),
    KEY `idx_group_available` (`dept_id`, `stock_id`, `sale_deadline`, `state`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '组别独立库存';

DROP TABLE IF EXISTS `t_warehouse`;
create TABLE t_warehouse
(
    id           BIGINT UNSIGNED AUTO_INCREMENT COMMENT 'ID',
    name         VARCHAR(100) NOT NULL COMMENT '仓库名称',
    code         VARCHAR(50) COMMENT '仓库编码',
    address      VARCHAR(255) COMMENT '仓库地址',
    manager_id   INT COMMENT '负责人ID',
    status       TINYINT         DEFAULT 1 COMMENT '状态',
    deleted      TINYINT         DEFAULT 0 COMMENT '是否删除',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
    `updated_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID',
    create_time  DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouse_code (code, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '仓库表';

INSERT INTO `t_warehouse` (`id`, `name`, `code`, `address`, `manager_id`, `status`, `deleted`, `created_by`, `updated_by`)
VALUES (1, '自社在庫', 'SELF', '自社倉庫', NULL, 1, 0, 1, 1),
       (2, 'ハンドル在庫', 'HANDLE', 'ハンドル倉庫', NULL, 1, 0, 1, 1);


DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`         VARCHAR(255)     NOT NULL COMMENT '商品名称',
    `english_name` VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',
    `brand_id`     BIGINT UNSIGNED  NOT NULL COMMENT '品牌ID',
    `series_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '系列ID',
    `category_id`  BIGINT UNSIGNED  NOT NULL COMMENT '商品类型ID',
    `maker_id`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '厂家ID',
    `description`  VARCHAR(1000)             DEFAULT NULL COMMENT '商品描述',
    `is_hot`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热门',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1上架2下架',
    `sort`         INT              NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_brand` (`brand_id`),
    KEY `idx_series` (`series_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_maker` (`maker_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='商品主表';

DROP TABLE IF EXISTS `t_goods_sku`;
CREATE TABLE `t_goods_sku`
(
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `goods_id`          BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_code`          VARCHAR(128)     NOT NULL COMMENT 'SKU编码/商品品番',
    `sku_name`          VARCHAR(255)              DEFAULT NULL COMMENT 'SKU展示名称',
    `price`             DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '销售价',
    `currency`          VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `cost_price`        DECIMAL(18, 2)            DEFAULT 0.00 COMMENT '成本价',
    `update_price`      DECIMAL(18, 2)            DEFAULT NULL COMMENT '待更新价格',
    `price_update_time` DATETIME                  DEFAULT NULL COMMENT '价格更新时间',
    `barcode`           VARCHAR(64)               DEFAULT NULL COMMENT '条码',
    `weight`            DECIMAL(10, 2)            DEFAULT NULL COMMENT '重量',
    `volume`            DECIMAL(10, 2)            DEFAULT NULL COMMENT '体积',
    `status`            TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`, `deleted`),
    KEY `idx_goods_id` (`goods_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='商品SKU表';

DROP TABLE IF EXISTS `t_goods_sku_spec`;

CREATE TABLE `t_goods_sku_spec`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `sku_id`      BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`    VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `spec_id`     INT UNSIGNED     NOT NULL COMMENT '规格ID',
    `spec_name`   VARCHAR(64)      NOT NULL COMMENT '规格名称',
    `spec_value`  VARCHAR(128)     NOT NULL COMMENT '规格值',
    `sort`        INT              NOT NULL DEFAULT 0,
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_id`, `spec_id`, `deleted`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_spec_id` (`spec_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='SKU规格属性表';

DROP TABLE IF EXISTS `t_goods_image`;
CREATE TABLE `t_goods_image`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_id`    BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_id`      BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`    VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `image_url`   VARCHAR(500)     NOT NULL COMMENT '图片地址',
    `sort`        INT              NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`),
    KEY idx_sku_id (sku_id),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='商品图片表';

DROP TABLE IF EXISTS `t_price_record`;
create TABLE `t_price_record`
(
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_id`          BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `goods_name`        VARCHAR(255)     NOT NULL COMMENT '商品名称',
    `english_name`      VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',
    `sku_id`            BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`          VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `old_price`         DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '更新前单价',
    `new_price`         DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '更新后单价',
    `currency`          VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `discount`          DECIMAL(5, 4)    NOT NULL DEFAULT 1.0000 COMMENT '折扣率',
    `price_update_time` DATETIME                  DEFAULT NULL COMMENT '价格更新时间',
    `operator_id`       BIGINT UNSIGNED  NOT NULL COMMENT '操作人id',
    `operator_name`     VARCHAR(64)      NOT NULL COMMENT '操作人名',
    `deleted`           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY idx_sku (sku_id),
    KEY idx_goods (goods_id),
    KEY idx_price_update_time (price_update_time),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '价格记录表';


DROP TABLE IF EXISTS `t_brand`;
create TABLE `t_brand`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`         varchar(255)     NOT NULL COMMENT '品牌名称',
    `english_name` VARCHAR(255)              DEFAULT NULL COMMENT '英文名',
    `image`        varchar(255)              DEFAULT NULL COMMENT '品牌封面图',
    `content`      text                      DEFAULT NULL COMMENT '品牌简介',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
    `deleted`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '品牌表';


DROP TABLE IF EXISTS `t_category`;
create TABLE `t_category`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`        varchar(255)     NOT NULL COMMENT '类型名称',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '商品类型表';


DROP TABLE IF EXISTS `t_series`;
create TABLE `t_series`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`         varchar(255)     NOT NULL COMMENT '系列名称',
    `english_name` VARCHAR(255)              DEFAULT NULL COMMENT '英文名',
    `brand_id`     BIGINT UNSIGNED  NOT NULL COMMENT '品牌ID',
    `content`      text                      DEFAULT NULL COMMENT '系列简介',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
    `deleted`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '商品系列表';


DROP TABLE IF EXISTS `t_maker`;
create TABLE `t_maker`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`        varchar(255)     NOT NULL COMMENT '厂家名称',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '厂家表';

DROP TABLE IF EXISTS `t_brand_maker_relation`;
CREATE TABLE `t_brand_maker_relation` (
      `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
      `brand_id` BIGINT UNSIGNED NOT NULL COMMENT '品牌ID',
      `maker_id` BIGINT UNSIGNED NOT NULL COMMENT '厂家ID',
      `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
      `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
      `updated_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID',
      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_brand_maker` (`brand_id`, `maker_id`),
      KEY `idx_brand` (`brand_id`),
      KEY `idx_maker` (`maker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌厂家关联表';

DROP TABLE IF EXISTS `t_series_brand_relation`;
CREATE TABLE `t_series_brand_relation` (
        `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
        `series_id` BIGINT UNSIGNED NOT NULL COMMENT '系列ID',
        `brand_id` BIGINT UNSIGNED NOT NULL COMMENT '品牌ID',
        `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
        `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
        `updated_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID',
        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        PRIMARY KEY (`id`),
        UNIQUE KEY `uk_series_brand` (`series_id`, `brand_id`),
        KEY `idx_series` (`series_id`),
        KEY `idx_brand` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系列品牌关联表';


DROP TABLE IF EXISTS `t_stock_type`;
create TABLE `t_stock_type`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`        varchar(255)     NOT NULL COMMENT '库存分类名称(常规品，不良品)',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:1启用0停用',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '库存分类表';

DROP TABLE IF EXISTS `t_request_form`;
create TABLE `t_request_form`
(
    `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `biz_no`         VARCHAR(64)      NOT NULL COMMENT '请求单号',
    `user_id`        BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `username`       VARCHAR(64)      NOT NULL COMMENT '用户名',
    `dept_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '申请部门ID',
    `dept_name`      VARCHAR(100)              DEFAULT NULL COMMENT '申请部门名称',
    `customer_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '客户ID',
    `customer_name`  VARCHAR(255)              DEFAULT NULL COMMENT '客户名称',
    `warehouse_id`   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '出库仓库ID',
    `source_order_id` BIGINT UNSIGNED           DEFAULT NULL COMMENT '出库单id',
    `total_qty`      INT(10)          NOT NULL DEFAULT 0 COMMENT '出库总数量',
    `request_qty`    INT(10)          NOT NULL DEFAULT 0 COMMENT '请求书写入数量',
    `total_amt`      DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `state`          TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '单据状态:0草稿1已提交2审核通过3已完成4已驳回5已取消',
    `approver_id`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '审核人ID',
    `approver_name`  VARCHAR(64)               DEFAULT NULL COMMENT '审核人',
    `approve_time`   DATETIME                  DEFAULT NULL COMMENT '审核时间',
    `approve_remark` VARCHAR(255)              DEFAULT NULL COMMENT '审核备注',
    `deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_biz_no (biz_no, deleted),
    KEY idx_user (user_id),
    KEY idx_source_order (source_order_id),
    KEY idx_customer (customer_id),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '请求书表';


DROP TABLE IF EXISTS `t_request_item`;
create TABLE `t_request_item`
(
    `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `request_id`      BIGINT UNSIGNED  NOT NULL COMMENT '请求单ID',
    `goods_id`        BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_id`          BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`        VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `goods_name`      VARCHAR(255)              DEFAULT NULL COMMENT '商品名称',
    `english_name`    VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',

    `brand_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '品牌ID',
    `brand_name`      VARCHAR(255)              DEFAULT NULL COMMENT '品牌名称',
    `series_id`       BIGINT UNSIGNED           DEFAULT NULL COMMENT '系列ID',
    `series_name`     VARCHAR(255)              DEFAULT NULL COMMENT '系列名称',
    `category_id`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '类型ID',
    `category_name`   VARCHAR(255)              DEFAULT NULL COMMENT '类型名称',
    `stock_type_id`   BIGINT UNSIGNED           DEFAULT NULL COMMENT '商品分类ID',
    `stock_type_name` VARCHAR(255)              DEFAULT NULL COMMENT '商品分类名称',
    `maker_id`        BIGINT UNSIGNED           DEFAULT NULL COMMENT '厂家ID',
    `maker_name`      VARCHAR(255)              DEFAULT NULL COMMENT '厂家名称',

    `warehouse_id`    BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '仓库ID',
    `price`           DECIMAL(18, 2)   NOT NULL DEFAULT 0.00 COMMENT '单价',
    `discount_price`  DECIMAL(18, 2)            DEFAULT NULL COMMENT '折扣价',
    `exchange_rate`   DECIMAL(18, 6)            DEFAULT NULL COMMENT '汇率',
    `currency`        VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `discount`        DECIMAL(5, 4)    NOT NULL DEFAULT 1.0000 COMMENT '折扣率',
    `request_qty`     INT(10)          NOT NULL DEFAULT 0 COMMENT '申请数量',
    `approve_qty`     INT(10)          NOT NULL DEFAULT 0 COMMENT '审核通过数量',
    `out_qty`         INT(10)          NOT NULL DEFAULT 0 COMMENT '实际出库数量',
    `total_amt`       DECIMAL(18, 2)            DEFAULT NULL COMMENT '总金额',
    `deposit_amt`     DECIMAL(18, 2)            DEFAULT NULL COMMENT '入金',
    `deposit_time`    DATETIME                  DEFAULT NULL COMMENT '入金时间',
    `deposit_fee`     DECIMAL(18, 2)            DEFAULT NULL COMMENT '入金手续费',
    `unpaid_amt`      DECIMAL(18, 2)            DEFAULT NULL COMMENT '未入金额',
    `stock_record_id` BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存流水ID',
    `state`           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '明细状态:0新增1删除',
    `remark`          VARCHAR(255)              DEFAULT NULL COMMENT '备注',
    `deleted`         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`      BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`      BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY idx_request_goods (request_id, goods_id),
    KEY idx_sku_id (sku_id),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_series_id` (`series_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_type_id` (`stock_type_id`),
    KEY `idx_maker_id` (`maker_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by),
    UNIQUE KEY uk_request_stock_record (request_id, stock_record_id, deleted)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '请求书商品明细表';

DROP TABLE IF EXISTS `t_customer`;
create TABLE `t_customer`
(
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    customer_code   VARCHAR(64)               DEFAULT NULL COMMENT '客户编号',
    name            VARCHAR(255)     NOT NULL COMMENT '客户名称',
    english_name    VARCHAR(255)              DEFAULT NULL COMMENT '英文名称',
    contact_person  VARCHAR(64)               DEFAULT NULL COMMENT '联系人',
    phone           VARCHAR(32)               DEFAULT NULL COMMENT '联系电话',
    email           VARCHAR(128)              DEFAULT NULL COMMENT '邮箱',
    country         VARCHAR(64)               DEFAULT NULL COMMENT '国家',
    city            VARCHAR(64)               DEFAULT NULL COMMENT '城市',
    address         VARCHAR(255)              DEFAULT NULL COMMENT '详细地址',
    level_id        TINYINT UNSIGNED          DEFAULT 1 COMMENT '客户等级',

    `owner_user_id` BIGINT UNSIGNED           DEFAULT NULL COMMENT '归属负责人ID',
    `owner_dept_id` BIGINT UNSIGNED           DEFAULT NULL COMMENT '归属部门ID',

    remark          VARCHAR(500)              DEFAULT NULL COMMENT '备注',
    status          TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态(1正常0停用)',
    deleted         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    create_time     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_customer_code (customer_code, deleted),
    KEY idx_name (name),
    KEY `idx_owner_user` (`owner_user_id`),
    KEY `idx_owner_dept` (`owner_dept_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '客户表';

DROP TABLE IF EXISTS `t_customer_level`;
CREATE TABLE `t_customer_level`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '等级ID',
    `name`        VARCHAR(64)      NOT NULL COMMENT '等级名称',
    `discount`    DECIMAL(5, 4)    NOT NULL DEFAULT 1.0000 COMMENT '默认折扣',
    `remark`      VARCHAR(255)              DEFAULT NULL COMMENT '备注',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='客户等级表';

DROP TABLE IF EXISTS `t_message`;
create TABLE `t_message`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `type`        TINYINT(3)       NOT NULL DEFAULT 0 COMMENT '类型:1=上新商品,2=新闻资讯,3=产品册',
    `user_id`     BIGINT UNSIGNED  NOT NULL COMMENT '用户ID',
    `message`     varchar(255)     NOT NULL COMMENT '消息',
    `source_id`   int(10)          NOT NULL DEFAULT 0 COMMENT '信息源ID',
    `is_read`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已读:0=否,1=是',
    `state`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发送状态:0待发送1已发送2失败',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY idx_user_read (user_id, is_read)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '消息通知表';

DROP TABLE IF EXISTS `t_config`;
create TABLE `t_config`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`        varchar(32)      NOT NULL COMMENT '变量名',
    `group`       varchar(32)      NOT NULL COMMENT '分组',
    `title`       varchar(100)     NOT NULL COMMENT '变量标题',
    `tip`         varchar(100)     NOT NULL COMMENT '变量描述',
    `type`        varchar(32)      NOT NULL COMMENT '类型:string,text,int,bool,array,datetime,date,file',
    `value`       text             NOT NULL COMMENT '变量值',
    `content`     text                      DEFAULT NULL COMMENT '变量字典数据',
    `deleted`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`  BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表';

INSERT INTO `t_config` (`name`, `group`, `title`, `tip`, `type`, `value`, `content`, `created_by`, `updated_by`)
VALUES ('request.form.template.default', 'request', 'Default request template', 'Fallback template for request form download', 'file', 'template/request_form_template_A.xlsx', NULL, 1, 1),
       ('request.form.template.A', 'request', 'Request template A', 'Template used by department code A', 'file', 'template/request_form_template_A.xlsx', NULL, 1, 1),
       ('request.form.template.B', 'request', 'Request template B', 'Template used by department code B', 'file', 'template/request_form_template_B.xlsx', NULL, 1, 1),
       ('request.form.template.C', 'request', 'Request template C', 'Template used by department code C', 'file', 'template/request_form_template_C.xlsx', NULL, 1, 1),
       ('knife_keywords', 'request', 'Knife category keywords',
        'Comma separated keywords used to identify knife categories for request item matching',
        'string', '厨刀,刀', NULL, 1, 1),
       ('handle_keywords', 'request', 'Handle category keywords',
        'Comma separated keywords used to identify handle categories for request item matching',
         'string', 'ハンドル,柄', NULL, 1, 1),
       ('stock.group.codes', 'stock', 'Stock group codes',
        'Department codes allowed to own group stock', 'string', 'A,B,C', NULL, 1, 1),
       ('perm.group.menu.json', 'permission', 'Group menu scope json',
        'Menu codes visible to each group department code', 'json',
        '{"A":["stock","stockA","stockOrder","stockType","stockRecord","priceRecord"],"B":["stock","stockB","stockOrder","stockType","stockRecord","priceRecord"],"C":["stock","stockC","stockOrder","stockType","stockRecord","priceRecord"]}',
        NULL, 1, 1);

INSERT INTO `t_config` (`name`, `group`, `title`, `tip`, `type`, `value`, `content`, `created_by`, `updated_by`)
VALUES('stock.group.codes', 'stock', 'Stock group codes',
       'Department codes allowed to own group stock', 'string', 'A,B,C', NULL, 1, 1),
      ('perm.group.menu.json', 'permission', 'Group menu scope json',
       'Menu codes visible to each group department code', 'json',
       '{"A":["stock","stockA","stockOrder","stockType","stockRecord","priceRecord"],"B":["stock","stockB","stockOrder","stockType","stockRecord","priceRecord"],"C":["stock","stockC","stockOrder","stockType","stockRecord","priceRecord"]}',
       NULL, 1, 1);


DROP TABLE IF EXISTS `t_operate_log`;
create TABLE `t_operate_log`
(
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`       BIGINT                    DEFAULT NULL COMMENT '操作用户ID',
    `username`      VARCHAR(64)               DEFAULT NULL COMMENT '操作用户名',
    `module`        VARCHAR(100)              DEFAULT NULL COMMENT '模块',
    `operation`     VARCHAR(100)              DEFAULT NULL COMMENT '操作类型',
    `method`        VARCHAR(255)              DEFAULT NULL COMMENT '请求方法',
    `request_url`   VARCHAR(255)              DEFAULT NULL COMMENT '请求URL',
    `request_ip`    VARCHAR(64)               DEFAULT NULL COMMENT '请求IP',
    `request_param` TEXT                      DEFAULT NULL COMMENT '请求参数',
    `response_data` TEXT                      DEFAULT NULL COMMENT '返回数据',
    `status`        TINYINT                   DEFAULT 1 COMMENT '状态 1成功 0失败',
    `error_msg`     TEXT                      DEFAULT NULL COMMENT '错误信息',
    `cost_time`     INT                       DEFAULT NULL COMMENT '执行时间(ms)',
    `deleted`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`    BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';
