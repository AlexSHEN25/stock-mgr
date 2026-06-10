-- Migrate legacy self stock permission codes to stock naming.
-- Run after backing up the database.

START TRANSACTION;

UPDATE t_permission
SET code = 'MENU_STOCK_SELF',
    name = '在庫管理',
    path = '/stock/self',
    component = 'stock/self'
WHERE code = 'MENU_SELF_STOCK';

UPDATE t_permission
SET code = 'DATA_STOCK_SELF_READ'
WHERE code = 'DATA_SELF_STOCK_READ';

UPDATE t_permission
SET code = 'DATA_STOCK_SELF_WRITE'
WHERE code = 'DATA_SELF_STOCK_WRITE';

UPDATE t_config
SET value = '{"A":["stock","stockA","stockOrder","stockType","stockRecord","priceRecord"],"B":["stock","stockB","stockOrder","stockType","stockRecord","priceRecord"],"C":["stock","stockC","stockOrder","stockType","stockRecord","priceRecord"]}'
WHERE name = 'perm.group.menu.json';

COMMIT;
