-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学员ID',
    `target_id` BIGINT NOT NULL COMMENT '收藏目标ID',
    `type` INT NOT NULL COMMENT '收藏类型 1:设施 2:教练 3:课程 4:食谱',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_target_type` (`student_id`, `target_id`, `type`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
