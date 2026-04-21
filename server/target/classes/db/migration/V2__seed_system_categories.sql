INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '餐饮', 'restaurant', 100, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '餐饮');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '购物', 'shopping_cart', 99, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '购物');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '交通', 'directions_car', 98, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '交通');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '住房', 'home', 97, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '住房');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '娱乐', 'movie', 96, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '娱乐');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '医疗', 'local_hospital', 95, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '医疗');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '教育', 'school', 94, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '教育');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '通讯', 'phone', 93, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '通讯');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '工资', 'account_balance_wallet', 100, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '工资');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '奖金', 'emoji_events', 99, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '奖金');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '兼职', 'work_outline', 98, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '兼职');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '理财', 'savings', 97, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '理财');
