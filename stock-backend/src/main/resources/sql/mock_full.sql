SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- clean
TRUNCATE TABLE t_operate_log;
TRUNCATE TABLE t_config;
TRUNCATE TABLE t_message;
TRUNCATE TABLE t_request_item;
TRUNCATE TABLE t_request_form;
TRUNCATE TABLE t_goods_level_price;
TRUNCATE TABLE t_customer;
TRUNCATE TABLE t_customer_level;
TRUNCATE TABLE t_stock_record;
TRUNCATE TABLE t_stock_order_item;
TRUNCATE TABLE t_stock_order;
TRUNCATE TABLE t_price_record;
TRUNCATE TABLE t_stock;
TRUNCATE TABLE t_warehouse;
TRUNCATE TABLE t_stock_type;
TRUNCATE TABLE t_goods_image;
TRUNCATE TABLE t_goods_sku_spec;
TRUNCATE TABLE t_goods_sku;
TRUNCATE TABLE t_goods;
TRUNCATE TABLE t_brand_maker_relation;
TRUNCATE TABLE t_maker;
TRUNCATE TABLE t_series;
TRUNCATE TABLE t_category;
TRUNCATE TABLE t_brand;
TRUNCATE TABLE t_role_permission;
TRUNCATE TABLE t_user_role;
TRUNCATE TABLE t_permission;
TRUNCATE TABLE t_role;
TRUNCATE TABLE t_user_token;
TRUNCATE TABLE t_user;
TRUNCATE TABLE t_dept;

-- dept
INSERT INTO t_dept(id,parent_id,name,code,leader_id,sort,status,deleted,created_by,updated_by)
VALUES
(1,0,'システム部','SYS',1,1,1,0,1,1),
(2,0,'営業部','SALES',2,2,1,0,1,1),
(3,0,'倉庫部','WH',3,3,1,0,1,1);

-- user
INSERT INTO t_user(id,username,password,dept_id,salt,email,phone,avatar,status,deleted,created_by,updated_by)
VALUES
(1,'admin',MD5('admin123'),1,'salt_admin','admin@test.com','09000000001','/avatar/admin.png',1,0,1,1),
(2,'sales01',MD5('sales123'),2,'salt_sales01','sales01@test.com','09000000002','/avatar/sales01.png',1,0,1,1),
(3,'warehouse01',MD5('wh123'),3,'salt_wh01','warehouse01@test.com','09000000003','/avatar/warehouse01.png',1,0,1,1),
(4,'viewer01',MD5('viewer123'),1,'salt_viewer','viewer01@test.com','09000000004','/avatar/viewer01.png',1,0,1,1);

-- user token
INSERT INTO t_user_token(id,token,user_id,login_time,expire_time,login_ip,status,deleted,created_by,updated_by)
VALUES
(1,'token_admin_001',1,NOW(),DATE_ADD(NOW(),INTERVAL 7 DAY),'127.0.0.1',1,0,1,1),
(2,'token_sales_001',2,NOW(),DATE_ADD(NOW(),INTERVAL 7 DAY),'127.0.0.1',1,0,1,1);

-- role
INSERT INTO t_role(id,name,code,remark,status,deleted,created_by,updated_by)
VALUES
(1,'スーパー管理者','ROLE_SUPER_ADMIN','全メニュー・全読写権限',1,0,1,1),
(2,'一般ユーザー','ROLE_NORMAL_USER','在庫管理・顧客管理（閲覧のみ）',1,0,1,1);

-- permission
INSERT INTO t_permission(id,name,code,module,type,parent_id,path,sort,icon,component,status,deleted,created_by,updated_by)
VALUES
(1,'システム管理','MENU_SYSTEM','system',1,0,'/system',1,'setting','system/index',1,0,1,1),
(2,'ユーザー管理','MENU_USER','user',1,1,'/user',2,'user','user/index',1,0,1,1),
(3,'商品管理','MENU_GOODS','goods',1,1,'/goods',3,'goods','goods/index',1,0,1,1),
(4,'在庫管理','MENU_STOCK','stock',1,1,'/stock',4,'stock','stock/index',1,0,1,1),
(5,'顧客管理','MENU_CUSTOMER','customer',1,1,'/customer',5,'customer','customer/index',1,0,1,1),
(6,'在庫管理-閲覧','DATA_STOCK_READ','stock',2,4,'/api/stock/**',1,'api','',1,0,1,1),
(7,'在庫管理-編集','DATA_STOCK_WRITE','stock',2,4,'/api/stock/**',2,'api','',1,0,1,1),
(8,'顧客管理-閲覧','DATA_CUSTOMER_READ','customer',2,5,'/api/customer/**',1,'api','',1,0,1,1),
(9,'顧客管理-編集','DATA_CUSTOMER_WRITE','customer',2,5,'/api/customer/**',2,'api','',1,0,1,1);

-- user role
INSERT INTO t_user_role(user_id,role_id,deleted,created_by,updated_by)
VALUES
(1,1,0,1,1),
(2,2,0,1,1),
(3,2,0,1,1),
(4,2,0,1,1);

-- role permission
INSERT INTO t_role_permission(role_id,permission_id,deleted,created_by,updated_by)
VALUES
(1,1,0,1,1),(1,2,0,1,1),(1,3,0,1,1),(1,4,0,1,1),(1,5,0,1,1),
(1,6,0,1,1),(1,7,0,1,1),(1,8,0,1,1),(1,9,0,1,1),
(2,4,0,1,1),(2,5,0,1,1),
(2,6,0,1,1),(2,8,0,1,1);

-- master data
INSERT INTO t_brand(id,name,english_name,image,content,status,deleted,created_by,updated_by)
VALUES
(1,'サクラ','SAKURA','/img/brand/sakura.png','日本ブランド',1,0,1,1),
(2,'富士','FUJI','/img/brand/fuji.png','定番ブランド',1,0,1,1);

INSERT INTO t_category(id,name,status,deleted,created_by,updated_by)
VALUES
(1,'食品',1,0,1,1),
(2,'日用品',1,0,1,1);

INSERT INTO t_series(id,name,english_name,brand_id,content,status,deleted,created_by,updated_by)
VALUES
(1,'春シリーズ','SPRING',1,'春限定',1,0,1,1),
(2,'標準シリーズ','STANDARD',2,'定番',1,0,1,1);

INSERT INTO t_maker(id,name,status,deleted,created_by,updated_by)
VALUES
(1,'東京工場',1,0,1,1),
(2,'大阪工場',1,0,1,1);

INSERT INTO t_brand_maker_relation(id,brand_id,maker_id,deleted,created_by,updated_by)
VALUES
(1,1,1,0,1,1),
(2,2,2,0,1,1);

INSERT INTO t_stock_type(id,name,status,deleted,created_by,updated_by)
VALUES
(1,'通常品',1,0,1,1),
(2,'不良品',1,0,1,1);

INSERT INTO t_warehouse(id,name,code,address,manager_id,status,deleted,created_by,updated_by)
VALUES
(1,'東京倉庫','WH-TYO','東京都港区',3,1,0,1,1),
(2,'大阪倉庫','WH-OSA','大阪市中央区',3,1,0,1,1);

-- goods / sku / spec / image
INSERT INTO t_goods(id,name,english_name,brand_id,series_id,category_id,maker_id,description,is_hot,sort,status,deleted,created_by,updated_by)
VALUES
(1,'抹茶クッキー','Matcha Cookie',1,1,1,1,'人気商品',1,10,1,0,1,1),
(2,'洗剤A','Detergent A',2,2,2,2,'日用品',0,20,1,0,1,1);

INSERT INTO t_goods_sku(id,goods_id,sku_code,sku_name,price,currency,cost_price,update_price,price_update_time,barcode,weight,volume,status,deleted,created_by,updated_by)
VALUES
(1,1,'SKU-MC-001','抹茶クッキー-小',298.00,'JPY',180.00,299.00,NOW(),'4901000000011',0.10,0.30,1,0,1,1),
(2,1,'SKU-MC-002','抹茶クッキー-大',598.00,'JPY',360.00,599.00,NOW(),'4901000000012',0.20,0.60,1,0,1,1),
(3,2,'SKU-DT-001','洗剤A-詰替',420.00,'JPY',250.00,425.00,NOW(),'4902000000001',0.50,0.80,1,0,1,1);

INSERT INTO t_goods_sku_spec(id,sku_id,sku_code,spec_id,spec_name,spec_value,sort,deleted,created_by,updated_by)
VALUES
(1,1,'SKU-MC-001',1,'容量','100g',1,0,1,1),
(2,2,'SKU-MC-002',1,'容量','250g',1,0,1,1),
(3,3,'SKU-DT-001',1,'容量','500ml',1,0,1,1);

INSERT INTO t_goods_image(id,goods_id,sku_id,sku_code,image_url,sort,deleted,created_by,updated_by)
VALUES
(1,1,1,'SKU-MC-001','/img/goods/mc-001-main.png',1,0,1,1),
(2,1,2,'SKU-MC-002','/img/goods/mc-002-main.png',1,0,1,1),
(3,2,3,'SKU-DT-001','/img/goods/dt-001-main.png',1,0,1,1);

-- stock
INSERT INTO t_stock(id,goods_id,goods_name,sku_id,sku_code,warehouse_id,current_qty,lock_qty,price,currency,price_update_time,stock_type_id,status,version,deleted,created_by,updated_by)
VALUES
(1,1,'抹茶クッキー',1,'SKU-MC-001',1,120,10,298.00,'JPY',NOW(),1,1,0,0,1,1),
(2,1,'抹茶クッキー',2,'SKU-MC-002',1,80,5,598.00,'JPY',NOW(),1,1,0,0,1,1),
(3,2,'洗剤A',3,'SKU-DT-001',2,200,20,420.00,'JPY',NOW(),1,1,0,0,1,1);

-- customer / level
INSERT INTO t_customer_level(id,name,discount,remark,status,deleted,created_by,updated_by)
VALUES
(1,'一般',1.0000,'標準価格',1,0,1,1),
(2,'VIP',0.9000,'優待価格',1,0,1,1);

INSERT INTO t_customer(id,customer_code,name,english_name,contact_person,phone,email,country,city,address,level_id,owner_user_id,owner_dept_id,remark,status,deleted,created_by,updated_by)
VALUES
(1,'CUST-001','山田商事','Yamada Trading','山田太郎','0311111111','yamada@test.com','JP','東京','港区1-1-1',1,2,2,'主要顧客',1,0,1,1),
(2,'CUST-002','大阪物産','Osaka Bussan','佐藤花子','0666666666','osaka@test.com','JP','大阪','中央区2-2-2',2,2,2,'VIP顧客',1,0,1,1);

INSERT INTO t_goods_level_price(id,goods_id,sku_id,sku_code,level_id,price,currency,discount,effective_time,expire_time,status,deleted,created_by,updated_by)
VALUES
(1,1,1,'SKU-MC-001',2,268.00,'JPY',0.9000,NOW(),DATE_ADD(NOW(),INTERVAL 365 DAY),1,0,1,1),
(2,2,3,'SKU-DT-001',2,378.00,'JPY',0.9000,NOW(),DATE_ADD(NOW(),INTERVAL 365 DAY),1,0,1,1);

-- request form / item
INSERT INTO t_request_form(id,biz_no,user_id,username,dept_id,dept_name,customer_id,customer_name,warehouse_id,total_qty,request_qty,total_amt,state,approver_id,approver_name,approve_time,approve_remark,deleted,created_by,updated_by)
VALUES
(1,'REQ-20260507-001',2,'sales01',2,'営業部',1,'山田商事',1,30,30,10740.00,2,1,'admin',NOW(),'承認済み',0,2,1),
(2,'REQ-20260507-002',2,'sales01',2,'営業部',2,'大阪物産',2,20,20,7560.00,1,NULL,NULL,NULL,NULL,0,2,2);

INSERT INTO t_request_item(id,request_id,goods_id,sku_id,sku_code,goods_name,english_name,brand_id,brand_name,series_id,series_name,category_id,category_name,stock_type_id,stock_type_name,maker_id,maker_name,warehouse_id,price,exchange_rate,currency,discount,request_qty,approve_qty,out_qty,total_amt,deposit_amt,deposit_time,stock_record_id,remark,deleted,created_by,updated_by)
VALUES
(1,1,1,1,'SKU-MC-001','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',1,298.00,1.000000,'JPY',1.0000,10,10,10,2980.00,0.00,NULL,1,'即納',0,2,1),
(2,1,1,2,'SKU-MC-002','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',1,598.00,1.000000,'JPY',1.0000,20,20,20,11960.00,0.00,NULL,2,'即納',0,2,1),
(3,2,2,3,'SKU-DT-001','洗剤A','Detergent A',2,'富士',2,'標準シリーズ',2,'日用品',1,'通常品',2,'大阪工場',2,420.00,1.000000,'JPY',0.9000,20,0,0,7560.00,500.00,NOW(),NULL,'審査待ち',0,2,2);

-- stock order / item / record
INSERT INTO t_stock_order(id,order_no,order_type,warehouse_id,source_type,source_id,total_qty,stock_type_id,state,requester_id,requester_name,operator_id,operator_name,remark,approver_id,approver_name,approve_time,version,finish_time,deleted,created_by,updated_by)
VALUES
(1,'SO-20260507-001',2,1,3,1,30,1,2,2,'sales01',3,'warehouse01','REQ連動',1,'admin',NOW(),1,NOW(),0,3,1),
(2,'SO-20260507-002',1,2,4,NULL,50,1,2,3,'warehouse01',3,'warehouse01','手動入庫',1,'admin',NOW(),1,NOW(),0,3,1);

INSERT INTO t_stock_order_item(id,order_id,goods_id,sku_id,sku_code,goods_name,english_name,brand_id,brand_name,series_id,series_name,category_id,category_name,stock_type_id,stock_type_name,maker_id,maker_name,before_qty,change_qty,after_qty,price,currency,remark,deleted,created_by,updated_by)
VALUES
(1,1,1,1,'SKU-MC-001','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',120,-10,110,298.00,'JPY','出庫',0,3,1),
(2,1,1,2,'SKU-MC-002','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',80,-20,60,598.00,'JPY','出庫',0,3,1),
(3,2,2,3,'SKU-DT-001','洗剤A','Detergent A',2,'富士',2,'標準シリーズ',2,'日用品',1,'通常品',2,'大阪工場',200,50,250,420.00,'JPY','入庫',0,3,1);

INSERT INTO t_stock_record(id,biz_no,order_id,order_item_id,stock_id,goods_id,sku_id,sku_code,goods_name,english_name,brand_id,brand_name,series_id,series_name,category_id,category_name,stock_type_id,stock_type_name,maker_id,maker_name,warehouse_id,before_qty,change_qty,after_qty,order_type,source_type,price,currency,price_update_time,customer_id,customer_name,requester_id,requester_name,operator_id,operator_name,remark,deleted,created_by,updated_by)
VALUES
(1,'SR-20260507-001',1,1,1,1,1,'SKU-MC-001','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',1,120,-10,110,2,3,298.00,'JPY',NOW(),1,'山田商事',2,'sales01',3,'warehouse01','出庫記録',0,3,1),
(2,'SR-20260507-002',1,2,2,1,2,'SKU-MC-002','抹茶クッキー','Matcha Cookie',1,'サクラ',1,'春シリーズ',1,'食品',1,'通常品',1,'東京工場',1,80,-20,60,2,3,598.00,'JPY',NOW(),1,'山田商事',2,'sales01',3,'warehouse01','出庫記録',0,3,1),
(3,'SR-20260507-003',2,3,3,2,3,'SKU-DT-001','洗剤A','Detergent A',2,'富士',2,'標準シリーズ',2,'日用品',1,'通常品',2,'大阪工場',2,200,50,250,1,4,420.00,'JPY',NOW(),NULL,NULL,3,'warehouse01',3,'warehouse01','入庫記録',0,3,1);

-- price record
INSERT INTO t_price_record(id,goods_id,goods_name,english_name,sku_id,sku_code,old_price,new_price,currency,discount,price_update_time,operator_id,operator_name,deleted,created_by,updated_by)
VALUES
(1,1,'抹茶クッキー','Matcha Cookie',1,'SKU-MC-001',280.00,298.00,'JPY',1.0000,NOW(),1,'admin',0,1,1),
(2,2,'洗剤A','Detergent A',3,'SKU-DT-001',400.00,420.00,'JPY',1.0000,NOW(),1,'admin',0,1,1);

-- message
INSERT INTO t_message(id,type,user_id,message,source_id,is_read,state,deleted,created_by,updated_by)
VALUES
(1,1,2,'新しい申請が承認されました',1,0,1,0,1,1),
(2,2,3,'在庫調整を実施してください',2,0,1,0,1,1);

-- config
INSERT INTO t_config(id,name,`group`,title,tip,type,value,content,deleted,created_by,updated_by)
VALUES
(1,'system.theme','ui','テーマ','画面テーマ','string','light','["light","dark"]',0,1,1),
(2,'stock.warn.threshold','stock','在庫警戒値','在庫不足しきい値','int','20',NULL,0,1,1);

-- operate log
INSERT INTO t_operate_log(id,user_id,username,module,operation,method,request_url,request_ip,request_param,response_data,status,error_msg,cost_time,deleted,created_by,updated_by)
VALUES
(1,1,'admin','schema','GET','GET','/api/schema/menu','127.0.0.1','{}','{"ok":true}',1,NULL,12,0,1,1),
(2,2,'sales01','request','CREATE','POST','/api/requestForm','127.0.0.1','{"bizNo":"REQ-20260507-002"}','{"ok":true}',1,NULL,35,0,2,2);

SET FOREIGN_KEY_CHECKS = 1;





