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
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '仓库表';


DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`         VARCHAR(255)     NOT NULL COMMENT '商品名称',
    `english_name` VARCHAR(255)              DEFAULT NULL COMMENT '英文品名',
    `brand_id`     BIGINT UNSIGNED  NOT NULL COMMENT '品牌ID',
    `series_id`    BIGINT UNSIGNED  NOT NULL COMMENT '系列ID',
    `category_id`  BIGINT UNSIGNED  NOT NULL COMMENT '商品类型ID',
    `maker_id`     BIGINT UNSIGNED  NOT NULL COMMENT '厂家ID',
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
CREATE TABLE t_brand_maker_relation
(
    `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    brand_id     BIGINT UNSIGNED  NOT NULL COMMENT '品牌ID',
    maker_id     BIGINT UNSIGNED  NOT NULL COMMENT '厂家ID',
    deleted      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by` BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by` BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    create_time  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_brand_maker (brand_id, maker_id),
    KEY idx_brand (brand_id),
    KEY idx_maker (maker_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='品牌厂家关联表';


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
    `customer_id`    BIGINT UNSIGNED  NOT NULL COMMENT '客户ID',
    `customer_name`  VARCHAR(255)     NOT NULL COMMENT '客户名称',
    `warehouse_id`   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '出库仓库ID',
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
    `exchange_rate`   DECIMAL(18, 6)            DEFAULT NULL COMMENT '汇率',
    `currency`        VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `discount`        DECIMAL(5, 4)    NOT NULL DEFAULT 1.0000 COMMENT '折扣率',
    `request_qty`     INT(10)          NOT NULL DEFAULT 0 COMMENT '申请数量',
    `approve_qty`     INT(10)          NOT NULL DEFAULT 0 COMMENT '审核通过数量',
    `out_qty`         INT(10)          NOT NULL DEFAULT 0 COMMENT '实际出库数量',
    `total_amt`       DECIMAL(18, 2)            DEFAULT NULL COMMENT '总金额',
    `deposit_amt`     DECIMAL(18, 2)            DEFAULT NULL COMMENT '定金',
    `deposit_time`    DATETIME                  DEFAULT NULL COMMENT '定金时间',
    `stock_record_id` BIGINT UNSIGNED           DEFAULT NULL COMMENT '库存流水ID',
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
    KEY idx_updated_by (updated_by)
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


DROP TABLE IF EXISTS `t_goods_level_price`;
CREATE TABLE `t_goods_level_price`
(
    `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `goods_id`       BIGINT UNSIGNED  NOT NULL COMMENT '商品ID',
    `sku_id`         BIGINT UNSIGNED  NOT NULL COMMENT 'SKU ID',
    `sku_code`       VARCHAR(128)              DEFAULT NULL COMMENT '商品品番',
    `level_id`       INT UNSIGNED     NOT NULL COMMENT '客户等级ID',
    `price`          DECIMAL(18, 2)   NOT NULL COMMENT '等级专属价格',
    `currency`       VARCHAR(8)       NOT NULL DEFAULT 'JPY' COMMENT '币种',
    `discount`       DECIMAL(5, 4)             DEFAULT NULL COMMENT '等级折扣率(可选)',
    `effective_time` DATETIME                  DEFAULT NULL COMMENT '生效时间',
    `expire_time`    DATETIME                  DEFAULT NULL COMMENT '失效时间',
    `status`         TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
    `created_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '创建人ID',
    `updated_by`     BIGINT UNSIGNED           DEFAULT NULL COMMENT '更新人ID',
    `create_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_sku_level (sku_id, level_id, deleted),
    KEY `idx_level` (`level_id`),
    KEY idx_created_by (created_by),
    KEY idx_updated_by (updated_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='客户等级商品价格表';


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


