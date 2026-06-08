SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Current reusable development snapshot.
-- Runtime logs and active login tokens are intentionally excluded.
-- Obsolete goods-level-price permissions are intentionally excluded.

-- clean
TRUNCATE TABLE t_operate_log;
TRUNCATE TABLE t_config;
TRUNCATE TABLE t_message;
TRUNCATE TABLE t_request_item;
TRUNCATE TABLE t_request_form;
TRUNCATE TABLE t_customer;
TRUNCATE TABLE t_customer_level;
TRUNCATE TABLE t_stock_record;
TRUNCATE TABLE t_group_stock;
TRUNCATE TABLE t_stock_batch;
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

-- current database data
INSERT INTO `t_dept` (`id`, `parent_id`, `name`, `code`, `leader_id`, `sort`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,0,'Aグループ','A',NULL,1,1,0,1,1,NOW(),NOW());
INSERT INTO `t_dept` (`id`, `parent_id`, `name`, `code`, `leader_id`, `sort`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,0,'Bグループ','B',NULL,2,1,0,1,1,NOW(),NOW());
INSERT INTO `t_dept` (`id`, `parent_id`, `name`, `code`, `leader_id`, `sort`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,0,'Cグループ','C',NULL,3,1,0,1,1,NOW(),NOW());
INSERT INTO `t_warehouse` (`id`, `name`, `code`, `address`, `manager_id`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'自社在庫','SELF','自社倉庫',NULL,1,0,1,1,NOW(),NOW());
INSERT INTO `t_warehouse` (`id`, `name`, `code`, `address`, `manager_id`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'ハンドル在庫','HANDLE','ハンドル倉庫',NULL,1,0,1,1,NOW(),NOW());
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'藤次郎','TOJIRO','/upload/brand/mpvzbt5335f576.png','藤次郎作为日本10大厨刀品牌之一，厨刀在传统“夹钢”工艺基础上专注于特种钢材的开发，采用特殊的钴合金“DP法”以防止内部脱碳，使刀具锋利、坚韧、防腐蚀，连获日本政府各类产品设计大奖，并在日本厨刀厂商中唯一取得ISO9001国际认证和ISO14001环境体系认证。其高档产品更是采用了高速粉末钢系列和63层大马士革钢。藤次郎厨刀开刃角度是15度，刃的前部分为两段，一段是15-20度向25度过度的“小刃止”；另一段是最前端，刃幅0.02-0.2mm的“刃先”，是30-35度。在切割试验机上实验，对比直接研磨成15度的\"刃先\",切割锋利度无差异，但锋利度耐久性延长。.\n\n1953年 \"建立Fujitora农场设备。开始制造农用设备的机器零件和刀片。\"\n1955年 开始生产烹饪刀具。\n1953年 \"建立Fujitora农场设备。开始制造农用设备的机器零件和刀片。\"\n1964年 成立富士达实业有限公司 资本基金为200万日元。\n1965年 资本扩张至300万日元。\n1968年 吉田工厂的新建设。制造部迁移到新工厂。\n1969年 资本扩张至600万日元。\n1970年 我们的产品被认为是新泻县的优秀推荐产品。\n1971年 收购吉田工厂扩建1500m2土地。\n1972年 吉田第二工厂的建设完成。\n1973年 资本扩张至1100万日元。\n1978年 吉田第三工厂的建设完成。\n1980年 我们的产品荣获MITI优良设计奖                                                                                                                \n1985年 业务总部搬迁到吉田工厂。\n1988年 吉田第4工厂的建设完成。销售部迁入现任总部。\n1992年 业务总部搬迁到现任总部。\n1993年 吉田工厂专用淬火设施的新建设。\n1994年 在吉田工厂扩建抛光设施。\n1995年 与German Dick GmbH合作开始合作刀具。\n1996年 扩大总部仓库。Tojiro被授予了MITI的优良设计奖和长寿命设计奖。\n1998年 吉田第三工厂的扩建完成。\n2000年 Tojiro-Pro SD日本刀系列被MITI授予2000年优良设计奖。\n2001年 赞助2次法国食品文化中心的比赛。\n2003年 2000认证ISO 9001。\n公司logo\n\n2004年 第一次展览在法兰克福展览中心Ambiente。认证ISO14001。\n2005年 董事长藤井弘先生去世。（72岁）\n2006年 收购吉田工厂扩建土地1500m2。\n2008年 在吉田南厂的“Tojiro刀工作室”的建设完成。\n2010年 在吉田南厂的抛光和清洗设施的扩大。\n2012年 \"Tojiro ORIGAMI获得IF设计奖2012（德国）。FUJI CUTLERY物流中心的装修完成。\"\n2013年 在吉田工厂采用制造工艺管理系统，安装数控磨床。',1,0,1,1,'2026-06-02 10:49:21','2026-06-02 10:49:21');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'堺孝行','SAKAITAKAYUKI','/upload/brand/mpvzxqllaab526.png','堺孝行（SAKAI TAKAYUKI）是青木刃物制作所旗下的品牌之一。位于日本著名的厨刀产地大阪府堺市。是享誉世界的堺打刃物中具有代表性的厨刀制造企业。堺打刃物距今约有600年的悠久历史，堺市出品的厨刀在日本专业料理人中具有极高的地位，专业厨刀市场占有率更是高达夸张的90%。\n\n堺孝行的主要历史：\n1947年 青木一郎商店创立\n1963年 改名创立青木刃物制作所\n1964年 堺孝行、堺菊孝商标登录\n1977年 创立三宝工厂\n1983年 堺正成是商标登录\n\n\n旗下知名匠人：\n1. 富樫憲治  鍛冶師 伝統工芸士\n2. 土井逸夫  鍛冶師\n3. 西村功  研磨师 伝統工芸士\n4. 土佐廣次  研磨师 伝統工芸士\n5. 窪田美知子 雕刻师\n\n堺孝行是将手工刀品牌化和市场化的业界代表，所内匠人平均从业年限30余年，品质和产量均有保障。',1,0,1,1,'2026-06-02 11:06:53','2026-06-02 11:06:53');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'二唐刃物','NIGARA','/upload/brand/mpw008yjed6c03.png',NULL,1,0,1,1,'2026-06-02 11:08:33','2026-06-02 11:08:33');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'初心','HATSUKOKORO','/upload/brand/mpw01yficfd290.jpg','我公司成立在2013年,並在2019年創立初心品牌,初心的日文读音为Hatsukokoro，是日文的音读，意思是最初的心愿和信念。\n\n唯有美食可以治愈人的心灵、而为了做出美味的美食、必不可少的就是一把好的厨刀。我们的初心就是给专业料理师和家庭主妇们提供性价比高的厨刀，一把不仅切味好、耐使用、握感舒服、而且价格优惠的厨刀。不忘此初心就是我们的公司理念。\n\n我们初心品牌根据钢材，锻造方法等分成了不同系列。每款系列名称是按照成品厨刀的外观与产品特点、用日文的自然现象、动物等名称命名。不仅生动而且便于记忆，消费者可以通过系列名了解到厨刀的性能之外，还可以感受的传统的日本文化。\n\n初心品牌的厨刀产于拥有日本传统厨刀产地的土佐、燕市、关市等。不仅保证了产品质量、也达到了消费者期待的产量。\n\n\n\n我们希望初心品牌的厨刀走进世界各地的厨房、餐厅，让料理师们为了自己心爱和重视的人做出美味的美食。让大家通过品尝美味的美食，拥有更美好的生活。',1,0,1,1,'2026-06-02 11:09:39','2026-06-02 11:09:39');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'NANIWA磨刀石','NANIWA','/upload/brand/mpw03tcza2dd1b.png','NANIWA研磨株式会社是一家产业链涉及金刚石工具、方形油石、树脂油石、涂附磨具、研磨油石、人造磨料、机床、作业工具、电动工具等一系列相关产品的制造和批发的综合产业公司。\n\n\n\n其中厨刀用磨刀石广泛被我们一般家庭及厨师业者所熟悉，具有众多知名系列磨刀石产品。因为该公司的磨刀石logo含有龙虾的标志，所以被国内用户广泛称为“日本龙虾砥石”。“龙虾砥石”广受世界各地刀具爱好者及厨师的好评，其中\"CHOSERA\"系列更是被各国厨师及厨刀研磨行业人员公认为性能最强的磨刀石之一。\n\n\n\n主要历史：\n\n1941年 NANIWA研磨工业所创立\n\n1952年 改名NANIWA研磨株式会社\n\n1960年 创设滋贺工厂\n\n1987年 创设滋贺物流中心\n\n2014年 创设株式会社NANIWA研磨贩卖\n\n\n\n\n\nGROUP公司：\n\n株式会社新潟ナニワ\n\n株式会社四国ナニワ\n\n株式会社ナニワ研磨販売\n\nナニワトイシ株式会社　滋賀工場\n\nナニワトイシ株式会社　滋賀物流センター',1,0,1,1,'2026-06-02 11:11:05','2026-06-02 11:11:05');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (6,'千叶','CHIBA','/upload/brand/mpw05sdx4760d3.png','品牌简介：\n千葉工業所创立于1969年，主要生产经营产品为业务用料理机器、家庭日用品、玩具制造。千叶的料理机器以「追求一流料理人的技术」为标准，产品问世后在专业料理师及料理店中取得了巨大的成功，提高厨房效率的同时还提供了专业厨师级别的切菜水准，各种切丝刨片切菜器常年畅销日本及海外。\n\n品牌历史：\n1964年 由長島三男首次在千叶县船桥市个人创业\n1969年 正式成立株式会社千叶工业所\n1976年 业务用切菜器开始制造销售\n2019年 品牌创立50周年',1,0,1,1,'2026-06-02 11:12:38','2026-06-02 11:12:38');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (7,'长谷川','HASEGAWA','/upload/brand/mpw07ckh1fd6d4.png','1.品牌介绍\n\n日本HASEGAWA专业砧板始于1955年，创立于千叶县八千代市，长谷川的烹饪用品，具有颜色区分，SIAA抗菌，轻量和高强度等其他无法模仿的特点，实现了高卫生性和烹饪作业的改善。\n\n长谷川从树脂等所有材料的研究到加工方法，发挥出了不被常识束缚的探求心和知识，开发出独创性的产品，致力于为提高各个领域的安心和安全做出贡献。\n\n创始人Uymamoto先生创造了第一个真正意义上的专业级抗菌砧板HASEGAWA。\n\n从早年的木质砧板，到后来的树脂抗菌砧板，但由于树脂抗菌砧板极易变形弯曲，\n\n后经过创新改善，将木质与树脂的特点完美的结合在了一起。使得HASEGAWA砧板既保证了砧板的抗菌性及切割性能，又保证了其长期的使用稳定性不变形。',1,0,1,1,'2026-06-02 11:13:50','2026-06-02 11:13:50');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (8,'中尾','NAKAO','/upload/brand/mpw092pl6d81b6.png','1.品牌介绍\n\n中尾铝制作所Nakao Alumi，日本厨具品牌，由创始人中尾富美夫成立于1958年（昭和33年）。昭和43年正式开始生产销售厨房用具，当时被广泛在大型有名的酒店·餐厅使用，所以也被称为是代表日本的锅的制造厂家。中尾始终坚持100％日本原料，100％日本生产，在材料、生产、技术、设计等维度不断突破，旨在为专业商用厨房和家庭消费者提供高品质产品。和一般的铝锅相比，板厚，打出来的效果也很好。深受大家的喜爱。现在在研制耐久性高质量的产品。\n\n\n\n铝的优点大致分为3个。\n\n\n\n①优良的耐腐蚀性能\n\n铝(铝制品)具有表面美丽不易脏，坚硬耐腐蚀的特性。铝暴露在空气中，会在表面形成一层薄薄的氧化膜。这个皮膜保护铝的表面，起到防止腐蚀的作用。\n\n\n\n②导热性能好\n\n要说热传导最好的材料是铜，但是考虑到重量和价格很难入手。其实铝也有不输的热传导。铝的导热率约为不锈钢的15倍，铁的3倍。因为水马上烧开，煮的菜慢慢加热，也关系到做菜的时间长短。\n\n\n\n③轻便性和易操作性\n\n铝的重量是铁和不锈钢的1/3，很轻，保养也很简单。平时的保养方法是用洗涤剂和海绵洗干净，然后擦干水分就可以了(用热水的话水越少越好)。即使发黑或烧焦了，用去污粉或毛巾轻轻擦一下就会消除。\n\n\n\n2.企业图片\n\n\n\n\n\n3.人气产品\n\n1型号:N-33 铝制雪平锅 尺寸 有15cm，18cm，21cm， 24cm等等多个尺寸\n\n\n\n\n2型号:ND-10  不锈钢雪平锅 尺寸 有15cm，18cm，21cm， 24cm等等多个尺寸  支持明火与电磁炉\n\n\n\n3型号:D-8  不锈钢雪平锅 尺寸 有15cm，18cm，21cm， 24cm等等多个尺寸\n\n支持明火与电磁炉  黄铜把手设计\n\n\n\n\n\n4型号:IHQP-3  不锈钢煮锅 尺寸 有15cm，21cm， 24cm\n\n支持明火与电磁炉 黄铜把手设计\n\n',1,0,1,1,'2026-06-02 11:15:11','2026-06-02 11:15:11');
INSERT INTO `t_brand` (`id`, `name`, `english_name`, `image`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (9,'其他','OTHER','/upload/brand/mpw0ail17b01a1.png',NULL,1,0,1,1,'2026-06-02 11:16:18','2026-06-02 11:16:18');
INSERT INTO `t_category` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'厨刀',1,0,1,1,'2026-06-02 11:20:59','2026-06-02 11:20:59');
INSERT INTO `t_category` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'厨房调理用品',1,0,1,1,'2026-06-02 11:21:29','2026-06-02 11:21:29');
INSERT INTO `t_category` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'磨刀石',1,0,1,1,'2026-06-02 11:21:51','2026-06-02 11:21:51');
INSERT INTO `t_category` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'柄',1,0,1,1,'2026-06-02 11:22:33','2026-06-02 11:22:33');
INSERT INTO `t_category` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'其他周边用品',1,0,1,1,'2026-06-02 11:22:37','2026-06-02 11:22:37');
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'system.theme','ui','テーマ','画面テーマ','string','light','[\"light\",\"dark\"]',0,1,1,'2026-05-25 15:42:06','2026-05-25 15:42:06');
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'stock.warn.threshold','stock','在庫警戒値','在庫不足しきい値','int','20',NULL,0,1,1,'2026-05-25 15:42:06','2026-05-25 15:42:06');
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'request.form.template.default','request','Default request template','Fallback template for request form download','file','template/request_form_template_A.xlsx',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'request.form.template.A','request','Request template A','Template used by department code A','file','template/request_form_template_A.xlsx',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'request.form.template.B','request','Request template B','Template used by department code B','file','template/request_form_template_B.xlsx',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (6,'request.form.template.C','request','Request template C','Template used by department code C','file','template/request_form_template_C.xlsx',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (7,'stock.group.codes','stock','Stock group codes','Department codes allowed to own group stock','string','A,B,C',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_config` (`id`, `name`, `group`, `title`, `tip`, `type`, `value`, `content`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (8,'perm.group.menu.json','permission','Group menu scope json','Menu codes visible to each group department code','json','{"A":["stock","selfStock","requestForm"],"B":["stock","selfStock"],"C":["stock","selfStock"]}',NULL,0,1,1,NOW(),NOW());
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'三悦',1,0,1,1,'2026-06-02 12:40:01','2026-06-02 12:40:01');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'二唐',1,0,1,1,'2026-06-02 12:40:12','2026-06-02 12:40:12');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'アシタ　二唐',1,0,1,1,'2026-06-02 12:40:18','2026-06-02 12:40:18');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'吉金',1,0,1,1,'2026-06-02 12:40:23','2026-06-02 12:40:23');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'増田',1,0,1,1,'2026-06-02 12:55:34','2026-06-02 12:55:34');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (6,'増谷刃物',1,0,1,1,'2026-06-02 12:55:43','2026-06-02 12:55:43');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (7,'佐治',1,0,1,1,'2026-06-02 12:55:59','2026-06-02 12:55:59');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (8,'山崎集民',1,0,1,1,'2026-06-02 12:56:17','2026-06-02 12:56:17');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (9,'黒崎',1,0,1,1,'2026-06-02 12:56:22','2026-06-02 12:56:22');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (10,'加藤',1,0,1,1,'2026-06-02 12:56:29','2026-06-02 12:56:29');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (11,'田中',1,0,1,1,'2026-06-02 12:56:42','2026-06-02 12:56:42');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (12,'祐成',1,0,1,1,'2026-06-02 12:56:49','2026-06-02 12:56:49');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (13,'藤次郎',1,0,1,1,'2026-06-02 12:56:57','2026-06-02 12:56:57');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (14,'本久一',1,0,1,1,'2026-06-02 12:57:02','2026-06-02 12:57:02');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (15,'明神',1,0,1,1,'2026-06-02 12:57:07','2026-06-02 12:57:07');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (16,'宗石',1,0,1,1,'2026-06-02 12:57:16','2026-06-02 12:57:16');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (17,'二瓶',1,0,1,1,'2026-06-02 12:57:21','2026-06-02 12:57:21');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (18,'鍛磨技巧',1,0,1,1,'2026-06-02 12:57:37','2026-06-02 12:57:37');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (19,'弥氏',1,0,1,1,'2026-06-02 12:57:41','2026-06-02 12:57:41');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (20,'迫田',1,0,1,1,'2026-06-02 12:57:46','2026-06-02 12:57:46');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (21,'忠義鍛造',1,0,1,1,'2026-06-02 12:57:51','2026-06-02 12:57:51');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (22,'丸章',1,0,1,1,'2026-06-02 12:57:57','2026-06-02 12:57:57');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (23,'世ノ元',1,0,1,1,'2026-06-02 12:58:06','2026-06-02 12:58:06');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (24,'岡村',1,0,1,1,'2026-06-02 12:58:11','2026-06-02 12:58:11');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (25,'公文',1,0,1,1,'2026-06-02 12:58:17','2026-06-02 12:58:17');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (26,'上村',1,0,1,1,'2026-06-02 12:58:22','2026-06-02 12:58:22');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (27,'ムラタ',1,0,1,1,'2026-06-02 12:58:27','2026-06-02 12:58:27');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (28,'柿本',1,0,1,1,'2026-06-02 12:58:32','2026-06-02 12:58:32');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (29,'中川',1,0,1,1,'2026-06-02 12:58:44','2026-06-02 12:58:44');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (30,'堺刀司製',1,0,1,1,'2026-06-02 12:58:58','2026-06-02 12:58:58');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (31,'関兼次',1,0,1,1,'2026-06-02 12:59:04','2026-06-02 12:59:04');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (32,'ハルノ',1,0,1,1,'2026-06-02 12:59:11','2026-06-02 12:59:11');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (33,'山脇刃物',1,0,1,1,'2026-06-02 12:59:15','2026-06-02 12:59:15');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (34,'梶原',1,0,1,1,'2026-06-02 12:59:30','2026-06-02 12:59:30');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (35,'石田',1,0,1,1,'2026-06-02 12:59:39','2026-06-02 12:59:39');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (36,'福本',1,0,1,1,'2026-06-02 12:59:47','2026-06-02 12:59:47');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (37,'山下',1,0,1,1,'2026-06-02 12:59:50','2026-06-02 12:59:50');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (38,'大野ナイフ',1,0,1,1,'2026-06-02 12:59:54','2026-06-02 12:59:54');
INSERT INTO `t_maker` (`id`, `name`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (39,'日の丸',1,0,1,1,'2026-06-02 13:00:00','2026-06-02 13:00:00');

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
    (22,'請求書明細閲覧','DATA_REQUEST_ITEM_READ','stock',2,4,'/api/requestItem/**',17,'api','',1,0,1,1,NOW(),NOW()),
    (23,'請求書明細編集','DATA_REQUEST_ITEM_WRITE','stock',2,4,'/api/requestItem/**',18,'api','',1,0,1,1,NOW(),NOW()),

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
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,1,1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,1,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,1,3,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,1,4,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,1,5,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (6,1,6,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (7,1,7,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (8,1,8,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (9,1,9,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (10,1,10,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (11,1,11,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (12,1,12,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (13,1,13,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (14,1,14,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (15,1,15,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (16,1,16,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (17,1,17,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (18,1,18,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (19,1,19,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (20,1,20,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (21,1,21,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (22,1,22,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (23,1,23,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (24,1,24,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (25,1,25,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (26,1,26,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (27,1,27,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (30,2,4,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (31,2,5,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (32,2,6,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (33,2,8,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (34,2,10,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (35,2,12,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (36,2,14,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (37,2,16,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (38,2,18,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (39,2,20,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (40,2,22,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (41,2,24,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (42,2,26,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (44,2,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (45,2,3,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (46,2,7,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (47,2,9,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (48,2,11,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (49,2,13,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (50,2,15,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (51,2,17,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (52,2,19,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (53,2,21,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (54,2,23,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (55,2,25,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (56,2,30,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (57,2,31,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (58,2,32,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (59,2,33,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (60,2,34,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (61,2,35,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (62,2,36,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_role_permission` (`id`, `role_id`, `permission_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (63,2,1,0,1,1,'2026-05-25 15:46:25','2026-05-25 15:46:25');

-- Latest ROLE_NORMAL_USER permissions:
-- all menus visible, all data readable, write only for own stock/request/customer flows and customer levels.
UPDATE t_role
SET remark = '全メニュー閲覧可。自分の入出庫伝票／入出庫明細、請求書／請求書明細、顧客、顧客ランクのみ登録・更新可'
WHERE code = 'ROLE_NORMAL_USER';

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
              OR p.path = '/api/requestItem' OR p.path LIKE '/api/requestItem/%'
              OR p.path = '/api/customer' OR p.path LIKE '/api/customer/%'
              OR p.path = '/api/customerLevel' OR p.path LIKE '/api/customerLevel/%'
          )
      )
  );
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'隼',NULL,4,NULL,1,0,1,1,'2026-06-02 12:30:13','2026-06-02 12:30:13');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'細氷',NULL,4,NULL,1,0,1,1,'2026-06-02 12:30:54','2026-06-02 12:30:54');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'砂嵐',NULL,4,NULL,1,0,1,1,'2026-06-02 12:31:19','2026-06-02 12:31:19');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'稲妻',NULL,4,NULL,1,0,1,1,'2026-06-02 12:31:29','2026-06-02 12:31:29');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'黒鷺',NULL,4,NULL,1,0,1,1,'2026-06-02 12:31:39','2026-06-02 12:31:39');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (6,'白鷺',NULL,4,NULL,1,0,1,1,'2026-06-02 12:31:51','2026-06-02 12:31:51');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (7,'雲影',NULL,4,NULL,1,0,1,1,'2026-06-02 12:32:21','2026-06-02 12:32:21');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (8,'氷紋',NULL,4,NULL,1,0,1,1,'2026-06-02 12:32:36','2026-06-02 12:32:36');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (9,'蜃気楼',NULL,4,NULL,1,0,1,1,'2026-06-02 12:32:49','2026-06-02 12:32:49');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (10,'刀',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:01','2026-06-02 12:33:01');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (11,'木漏れ日',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:12','2026-06-02 12:33:12');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (12,'夜明け',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:23','2026-06-02 12:33:23');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (13,'銀葉',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:32','2026-06-02 12:33:32');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (14,'黒風　',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:40','2026-06-02 12:33:40');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (15,'喜び',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:48','2026-06-02 12:33:48');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (16,'中華包丁',NULL,4,NULL,1,0,1,1,'2026-06-02 12:33:59','2026-06-02 12:33:59');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (17,'黒鯨',NULL,4,NULL,1,0,1,1,'2026-06-02 12:34:08','2026-06-02 12:34:08');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (18,'銀霜',NULL,4,NULL,1,0,1,1,'2026-06-02 12:34:16','2026-06-02 12:34:16');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (19,'彩り',NULL,4,NULL,1,0,1,1,'2026-06-02 12:34:38','2026-06-02 12:34:38');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (20,'流水',NULL,4,NULL,1,0,1,1,'2026-06-02 12:34:53','2026-06-02 12:34:53');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (21,'銀河34',NULL,4,NULL,1,0,1,1,'2026-06-02 12:35:12','2026-06-02 12:35:12');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (22,'黒風',NULL,4,NULL,1,0,1,1,'2026-06-02 12:35:35','2026-06-02 12:35:35');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (23,'黒熊',NULL,4,NULL,1,0,1,1,'2026-06-02 12:35:49','2026-06-02 12:35:49');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (24,'墨',NULL,4,NULL,1,0,1,1,'2026-06-02 12:35:58','2026-06-02 12:35:58');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (25,'漆黒',NULL,4,NULL,1,0,1,1,'2026-06-02 12:36:07','2026-06-02 12:36:07');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (26,'黒橡',NULL,4,NULL,1,0,1,1,'2026-06-02 12:36:17','2026-06-02 12:36:17');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (27,'黒波',NULL,4,NULL,1,0,1,1,'2026-06-02 12:36:39','2026-06-02 12:36:39');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (28,'黒鉄',NULL,4,NULL,1,0,1,1,'2026-06-02 12:37:07','2026-06-02 12:37:07');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (29,'光',NULL,4,NULL,1,0,1,1,'2026-06-02 12:37:14','2026-06-02 12:37:14');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (30,'孔雀 ',NULL,4,NULL,1,0,1,1,'2026-06-02 12:37:24','2026-06-02 12:37:24');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (31,'青鍛',NULL,4,NULL,1,0,1,1,'2026-06-02 12:37:32','2026-06-02 12:37:32');
INSERT INTO `t_series` (`id`, `name`, `english_name`, `brand_id`, `content`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (32,'火ノ鳥 ',NULL,4,NULL,1,0,1,1,'2026-06-02 12:37:39','2026-06-02 12:37:39');
INSERT INTO `t_user` (`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,'admin','53ed35e1ac326eec04db3a65aa7f0276',1,'salt_admin','admin@test.com','09000000001','/avatar/upload/1_0cf14549-8988-4f0d-9132-975344b88071.png',1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user` (`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,'sales01','285cb306e6caa304a5cdbf9d2f9bfacb',2,'salt_sales01','sales01@test.com','09000000002','/avatar/sales01.png',1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user` (`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,'warehouse01','8750063d034114ef8d4aca57898550dd',3,'salt_wh01','warehouse01@test.com','09000000003','/avatar/warehouse01.png',1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user` (`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,'viewer01','4fbc57eea3b37fdc30dc096dbc483176',1,'salt_viewer','viewer01@test.com','09000000004','/avatar/viewer01.png',1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user` (`id`, `username`, `password`, `dept_id`, `salt`, `email`, `phone`, `avatar`, `status`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,'sales02','256c07013bc6ec48d0d3483ad34592bf',2,'60e92491f5099c82bfe87aaf4a5247c6','sales02@handk.o','07711112222','/avatar/upload/mpq9wepm05c389.png',1,0,1,1,'2026-05-27 21:10:26','2026-05-29 10:58:38');
INSERT INTO `t_user_role` (`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (1,1,1,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user_role` (`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (2,2,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user_role` (`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (3,3,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user_role` (`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (4,4,2,0,1,1,'2026-05-25 15:42:05','2026-05-25 15:42:05');
INSERT INTO `t_user_role` (`id`, `user_id`, `role_id`, `deleted`, `created_by`, `updated_by`, `create_time`, `update_time`) VALUES (5,5,1,0,1,1,'2026-05-27 21:10:26','2026-05-27 21:10:26');

SET FOREIGN_KEY_CHECKS = 1;
