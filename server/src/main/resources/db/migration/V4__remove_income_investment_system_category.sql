-- 项目定义中“投资理财”属于支出分类，不应作为系统默认收入分类存在。
-- 处理已存在库：将系统收入下的“投资理财”引用迁移到“其他收入”，再删除该分类。

UPDATE book_transaction bt
JOIN category src
  ON bt.category_id = src.id
 AND src.user_id = 0
 AND src.type = 2
 AND src.name = '投资理财'
JOIN category dst
  ON dst.user_id = 0
 AND dst.type = 2
 AND dst.name = '其他收入'
SET bt.category_id = dst.id,
    bt.updated_at = NOW();

UPDATE template t
JOIN category src
  ON t.category_id = src.id
 AND src.user_id = 0
 AND src.type = 2
 AND src.name = '投资理财'
JOIN category dst
  ON dst.user_id = 0
 AND dst.type = 2
 AND dst.name = '其他收入'
SET t.category_id = dst.id,
    t.updated_at = NOW();

DELETE FROM category
WHERE user_id = 0
  AND type = 2
  AND name = '投资理财';
