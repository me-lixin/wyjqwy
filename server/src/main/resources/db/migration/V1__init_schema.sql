CREATE TABLE app_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一主键ID',
                          username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录账号，需唯一）',
                          password_hash VARCHAR(100) NOT NULL COMMENT '加密后的登录密码',
                          created_at DATETIME NOT NULL COMMENT '账号创建时间',
                          updated_at DATETIME NOT NULL COMMENT '账号信息最后更新时间'
) COMMENT='APP用户表';

CREATE TABLE category (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类唯一主键ID',
                          user_id BIGINT NOT NULL COMMENT '所属用户ID（关联app_user表）',
                          type TINYINT NOT NULL COMMENT '分类类型：例如 2-收入，1-支出',
                          name VARCHAR(30) NOT NULL COMMENT '分类名称（如：餐饮、交通、工资）',
                          icon VARCHAR(50) NULL COMMENT '分类图标的标识或路径',
                          sort INT NOT NULL DEFAULT 0 COMMENT '排序权重（用于前端UI自定义展示顺序）',
                          created_at DATETIME NOT NULL COMMENT '分类创建时间',
                          updated_at DATETIME NOT NULL COMMENT '分类最后更新时间'
) COMMENT='记账收支分类表';

CREATE TABLE book_transaction (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单记录唯一主键ID',
                                  user_id BIGINT NOT NULL COMMENT '所属用户ID（关联app_user表）',
                                  type TINYINT NOT NULL COMMENT '账单类型：例如 2-收入，1-支出',
                                  amount DECIMAL(18,2) NOT NULL COMMENT '交易金额（保留两位小数）',
                                  category_id BIGINT NOT NULL COMMENT '所属分类ID（关联category表）',
                                  note VARCHAR(200) NULL COMMENT '账单备注/说明',
                                  occurred_at DATETIME NOT NULL COMMENT '实际交易发生时间（用户选择的记账时间）',
                                  created_at DATETIME NOT NULL COMMENT '记录创建时间（系统记录时间）',
                                  updated_at DATETIME NOT NULL COMMENT '记录最后更新时间'
) COMMENT='记账流水明细表';

CREATE TABLE template (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板唯一主键ID',
                          user_id BIGINT NOT NULL COMMENT '所属用户ID（关联app_user表）',
                          type TINYINT NOT NULL COMMENT '模板类型：例如 2-收入，1-支出',
                          amount DECIMAL(18,2) NULL COMMENT '预设的快捷记账金额（可为空，由用户记账时输入）',
                          category_id BIGINT NOT NULL COMMENT '预设的分类ID（关联category表）',
                          note VARCHAR(200) NULL COMMENT '预设的快捷备注',
                          sort INT NOT NULL DEFAULT 0 COMMENT '排序权重（用于前端UI自定义快捷模板的展示顺序）',
                          created_at DATETIME NOT NULL COMMENT '模板创建时间',
                          updated_at DATETIME NOT NULL COMMENT '模板最后更新时间'
) COMMENT='快捷记账模板表';