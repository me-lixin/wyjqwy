-- 系统内置分类：user_id=0，name 为展示用中文，icon 为英文 key（与客户端 categoryIconForIconKey 一致）

-- 支出 type=1
INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '餐饮美食', 'food_dining', 100, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '餐饮美食');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '交通出行', 'transport', 99, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '交通出行');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '购物消费', 'shopping', 98, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '购物消费');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '居家生活', 'home_life', 97, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '居家生活');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '水电煤气', 'utilities', 96, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '水电煤气');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '话费网费', 'phone_bill', 95, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '话费网费');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '休闲娱乐', 'entertainment', 94, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '休闲娱乐');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '零食饮品', 'snacks_drinks', 93, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '零食饮品');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '护肤美妆', 'beauty', 92, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '护肤美妆');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '服饰鞋包', 'fashion', 91, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '服饰鞋包');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '医疗健康', 'medical', 90, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '医疗健康');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '学习教育', 'education', 89, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '学习教育');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '人情往来', 'social_relation', 88, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '人情往来');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '宠物开销', 'pets', 87, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '宠物开销');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '运动健身', 'sports', 86, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '运动健身');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '汽车开销', 'auto', 85, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '汽车开销');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '数码电器', 'digital', 84, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '数码电器');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '房租房贷', 'housing_loan', 83, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '房租房贷');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '金融保险', 'finance_insurance', 82, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '金融保险');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '其他支出', 'other_expense', 81, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '其他支出');

-- 收入 type=2
INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '职业薪水', 'salary', 100, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '职业薪水');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '兼职外快', 'part_time', 99, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '兼职外快');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '投资理财', 'investment', 98, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '投资理财');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '奖金福利', 'bonus', 97, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '奖金福利');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '收债回款', 'debt_collection', 96, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '收债回款');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '人情礼金', 'gift_money', 95, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '人情礼金');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '二手闲置', 'secondhand', 94, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '二手闲置');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '报销入账', 'reimbursement', 93, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '报销入账');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '营业收入', 'business_income', 92, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '营业收入');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 2, '其他收入', 'other_income', 91, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 2 AND name = '其他收入');
