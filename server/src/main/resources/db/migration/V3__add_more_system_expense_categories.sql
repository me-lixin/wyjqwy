-- 给已执行过历史迁移的库补充 4 个系统支出分类，并确保它们位于“其他支出”之前

-- 先统一尾部排序，避免同 sort 值导致展示顺序不稳定
UPDATE category SET sort = 89, updated_at = NOW() WHERE user_id = 0 AND type = 1 AND name = '汽车开销';
UPDATE category SET sort = 88, updated_at = NOW() WHERE user_id = 0 AND type = 1 AND name = '数码电器';
UPDATE category SET sort = 87, updated_at = NOW() WHERE user_id = 0 AND type = 1 AND name = '房租房贷';
UPDATE category SET sort = 86, updated_at = NOW() WHERE user_id = 0 AND type = 1 AND name = '金融保险';

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '投资理财', 'expense_investment', 85, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '投资理财');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '烟酒糖茶', 'tobacco_alcohol_tea', 84, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '烟酒糖茶');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '工作办公', 'work_office', 83, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '工作办公');

INSERT INTO category (user_id, type, name, icon, sort, created_at, updated_at)
SELECT 0, 1, '老人小孩', 'elder_child', 82, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM category WHERE user_id = 0 AND type = 1 AND name = '老人小孩');

UPDATE category SET sort = 81, updated_at = NOW()
WHERE user_id = 0 AND type = 1 AND name = '其他支出';
