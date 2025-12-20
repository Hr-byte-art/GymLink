-- 为 recipe 表添加营养分析字段
-- 执行前请备份数据

ALTER TABLE recipe
ADD COLUMN calories INT DEFAULT NULL COMMENT '热量(千卡)',
ADD COLUMN protein INT DEFAULT NULL COMMENT '蛋白质(克)',
ADD COLUMN carbs INT DEFAULT NULL COMMENT '碳水化合物(克)',
ADD COLUMN fat INT DEFAULT NULL COMMENT '脂肪(克)',
ADD COLUMN prep_time INT DEFAULT NULL COMMENT '准备时间(分钟)',
ADD COLUMN cook_time INT DEFAULT NULL COMMENT '烹饪时间(分钟)',
ADD COLUMN servings INT DEFAULT 1 COMMENT '份数',
ADD COLUMN difficulty VARCHAR(20) DEFAULT NULL COMMENT '难度(easy/medium/hard)';

-- 添加索引（可选，根据查询需求）
-- CREATE INDEX idx_recipe_calories ON recipe(calories);
-- CREATE INDEX idx_recipe_difficulty ON recipe(difficulty);
