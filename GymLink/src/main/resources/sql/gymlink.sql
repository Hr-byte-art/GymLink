/*
 Navicat Premium Dump SQL

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : gymlink

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 10/12/2025 20:43:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32132 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '王哈哈', '1008611', NULL, '2025-12-07 03:11:45');

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '发布管理员ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------

-- ----------------------------
-- Table structure for coach
-- ----------------------------
DROP TABLE IF EXISTS `coach`;
CREATE TABLE `coach`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `gender` tinyint(1) NULL DEFAULT 1 COMMENT '性别 1:男 2:女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `specialty` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '特长/专业方向',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人简介',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入驻时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1998396560747569154 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教练信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coach
-- ----------------------------
INSERT INTO `coach` VALUES (1001, 'coach1', '张教练', 1, '13800138001', NULL, 30, '力量训练', '专业力量训练教练', '2025-12-09 22:09:08');
INSERT INTO `coach` VALUES (1002, 'coach2', '李教练', 1, '13800138002', NULL, 35, '瑜伽', '专业瑜伽教练', '2025-12-09 22:09:08');
INSERT INTO `coach` VALUES (1003, 'coach3', '王教练', 2, '13800138003', NULL, 28, '有氧运动', '专业有氧运动教练', '2025-12-09 22:09:08');
INSERT INTO `coach` VALUES (1997598239422427137, 'zhuxixi', '朱教练', 2, '18328384562', NULL, 22, '瑜伽', '专业的女教练', '2025-12-07 17:25:09');
INSERT INTO `coach` VALUES (1998396560747569153, 'zzzzzzzz', '测试', 1, '', NULL, 25, '康复/矫正训练教练', '这个是测试的喔', '2025-12-09 22:17:23');

-- ----------------------------
-- Table structure for coach_appointment
-- ----------------------------
DROP TABLE IF EXISTS `coach_appointment`;
CREATE TABLE `coach_appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学员ID',
  `coach_id` bigint NOT NULL COMMENT '预约的教练ID',
  `appoint_time` datetime NOT NULL COMMENT '预约的日期时间',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态 0:待确认 1:已确认 2:已拒绝 3:已取消',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教练预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coach_appointment
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `experience_id` bigint NOT NULL COMMENT '关联的健身经验ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `user_role` int NOT NULL COMMENT '评论者角色 1:教练 2:学员',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论）',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '被回复的用户ID',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_experience_id`(`experience_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, '这是真的吗\n', 5, 1996238958257303554, 2, NULL, NULL, 0, '2025-12-08 22:46:14');
INSERT INTO `comment` VALUES (2, '你好\n', 5, 1996238958257303554, 2, 1, 1996238958257303554, 0, '2025-12-08 22:46:22');

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `coach_id` bigint NOT NULL COMMENT '关联教练ID',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程封面图',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程详情',
  `price` decimal(10, 2) NOT NULL COMMENT '课程价格',
  `duration` int NULL DEFAULT 60 COMMENT '课程时长(分钟)',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '初级' COMMENT '难度等级',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `type` char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程类别',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '测试', 1001, 'https://gym-link-1340059484.cos.ap-chengdu.myqcloud.com/course/2025/12/09/1.webp', '这是一个测试', 11.00, 62, '中级', '2025-12-09 22:51:09', '私教课程');

-- ----------------------------
-- Table structure for course_order
-- ----------------------------
DROP TABLE IF EXISTS `course_order`;
CREATE TABLE `course_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `student_id` bigint NOT NULL COMMENT '学员ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `coach_id` bigint NOT NULL COMMENT '冗余教练ID(方便查询)',
  `price` decimal(10, 2) NOT NULL COMMENT '成交价格',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '订单状态 1:已支付 2:已退款',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程购买订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_order
-- ----------------------------

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '器材名称',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '器材图片',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '放置位置',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '器材描述/使用说明',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 2:损坏/维护中',
  `total_count` int NULL DEFAULT 1 COMMENT '器材总数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `type` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身器材表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of equipment
-- ----------------------------
INSERT INTO `equipment` VALUES (1, '跑步机', 'https://gym-link-1340059484.cos.ap-chengdu.myqcloud.com/equipment/2025/12/07/1.webp', '西楼1号位', '跑步机', 1, 4, '2025-12-07 17:26:54', NULL);
INSERT INTO `equipment` VALUES (2, '测试', '', '测试', '测试', 2, 3, '2025-12-09 23:08:03', '2-2');

-- ----------------------------
-- Table structure for equipment_reservation
-- ----------------------------
DROP TABLE IF EXISTS `equipment_reservation`;
CREATE TABLE `equipment_reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学员ID',
  `equipment_id` bigint NOT NULL COMMENT '器材ID',
  `start_time` datetime NOT NULL COMMENT '开始使用时间',
  `end_time` datetime NOT NULL COMMENT '结束使用时间',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:预约成功 2:已取消 3:已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '器材预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of equipment_reservation
-- ----------------------------

-- ----------------------------
-- Table structure for experience
-- ----------------------------
DROP TABLE IF EXISTS `experience`;
CREATE TABLE `experience`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '经验内容(富文本)',
  `user_id` bigint NOT NULL COMMENT '发布者ID(教练或学员)',
  `user_role` tinyint(1) NOT NULL COMMENT '发布者角色 1:教练 2:学员',
  `view_count` bigint NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身经验/社区帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of experience
-- ----------------------------
INSERT INTO `experience` VALUES (5, '测试', '测试', 1996238958257303554, 2, 39, '2025-12-08 22:13:23');

-- ----------------------------
-- Table structure for experience_reaction
-- ----------------------------
DROP TABLE IF EXISTS `experience_reaction`;
CREATE TABLE `experience_reaction`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `experience_id` bigint UNSIGNED NOT NULL COMMENT '关联的帖子ID（外键关联帖子表）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '操作用户ID（外键关联用户表）',
  `reaction_type` tinyint NOT NULL COMMENT '反应类型：1=点赞，2=点不喜欢',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（如取消后重新操作）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_experience_user`(`experience_id` ASC, `user_id` ASC) USING BTREE COMMENT '唯一约束：同一用户对同一帖子只能有一个反应',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户维度查询索引（如“我的点赞记录”）',
  INDEX `idx_post_id`(`experience_id` ASC) USING BTREE COMMENT '帖子维度查询索引（如“帖子的点赞数”）'
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞/点不喜欢记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of experience_reaction
-- ----------------------------
INSERT INTO `experience_reaction` VALUES (17, 5, 1996238958257303554, 1, '2025-12-08 23:41:57', '2025-12-08 23:41:57');

-- ----------------------------
-- Table structure for recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `recharge_record`;
CREATE TABLE `recharge_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学员ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge_record
-- ----------------------------

-- ----------------------------
-- Table structure for recipe
-- ----------------------------
DROP TABLE IF EXISTS `recipe`;
CREATE TABLE `recipe`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜谱标题',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '菜谱内容(富文本)',
  `tags` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签(如:增肌,减脂)',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '发布人ID(管理员)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健身菜谱表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe
-- ----------------------------
INSERT INTO `recipe` VALUES (1, '测试', 'https://gym-link-1340059484.cos.ap-chengdu.myqcloud.com/recipe/2025/12/09/1.webp', '测试', '增肌食谱,维持期食谱,高蛋白食谱,力量训练专用', 1, '2025-12-09 23:24:05');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称/姓名',
  `gender` tinyint(1) NULL DEFAULT 1 COMMENT '性别 1:男 2:女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `height` decimal(5, 2) NULL DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重(kg)',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额(用于购买课程)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1998396163278544898 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学员/用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (2001, 'student1', '小明', 1, '13800138004', NULL, 175.00, 70.00, 0.00, '2025-12-09 22:09:08');
INSERT INTO `student` VALUES (2002, 'student2', '小红', 2, '13800138005', NULL, 165.00, 55.00, 0.00, '2025-12-09 22:09:08');
INSERT INTO `student` VALUES (2003, 'student3', '小刚', 1, '13800138006', NULL, 180.00, 80.00, 0.00, '2025-12-09 22:09:08');
INSERT INTO `student` VALUES (1996238958139863041, 'wanghaha', '王哈哈', 1, '17308006759', 'https://gym-link-1340059484.cos.ap-chengdu.myqcloud.com/avatar/student/2025/12/07/1996238958139863041.webp', 173.00, 69.00, 0.00, '2025-12-03 23:23:51');
INSERT INTO `student` VALUES (1998396163278544897, 'zzzzzzzz', '测试', 1, '', NULL, 173.00, 65.00, 0.00, '2025-12-09 22:15:48');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL COMMENT 'id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_delete` tinyint NOT NULL COMMENT '是否删除 0-否 1-是',
  `role` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色 0-管理员 1-教练 2-普通用户',
  `associated_user_id` bigint NOT NULL COMMENT '关联的用户id（role为学员就对应学员id....）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'admin', 1);
INSERT INTO `user` VALUES (1001, 'coach1', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'coach', 1001);
INSERT INTO `user` VALUES (1002, 'coach2', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'coach', 1002);
INSERT INTO `user` VALUES (1003, 'coach3', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'coach', 1003);
INSERT INTO `user` VALUES (2001, 'student1', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'student', 2001);
INSERT INTO `user` VALUES (2002, 'student2', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'student', 2002);
INSERT INTO `user` VALUES (2003, 'student3', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:09:08', 0, 'student', 2003);
INSERT INTO `user` VALUES (1994733200806113281, 'user', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-11-29 19:40:30', 0, 'student', 0);
INSERT INTO `user` VALUES (1996238958257303554, 'wanghaha', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-03 23:23:51', 0, 'student', 1996238958139863041);
INSERT INTO `user` VALUES (1997391004352200705, 'coach', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-07 03:41:40', 1, 'coach', 1997391004280897537);
INSERT INTO `user` VALUES (1997598239497924609, 'zhuxixi', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-07 17:25:09', 0, 'coach', 1997598239422427137);
INSERT INTO `user` VALUES (1998396163316293633, 'zzzzzzzz', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:15:48', 0, 'student', 1998396163278544897);
INSERT INTO `user` VALUES (1998396560747569154, 'zzzzzzzz', '07379f98ea7c6ccc9701d42cab74db77', 1, '2025-12-09 22:17:23', 0, 'coach', 1998396560747569153);

SET FOREIGN_KEY_CHECKS = 1;
