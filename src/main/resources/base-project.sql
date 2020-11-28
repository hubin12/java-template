/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50726
 Source Host           : localhost:3306
 Source Schema         : base-project

 Target Server Type    : MySQL
 Target Server Version : 50726
 File Encoding         : 65001

 Date: 28/11/2020 17:44:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_permission`;
CREATE TABLE `tb_permission`  (
  `id` bigint(20) NOT NULL COMMENT '权限id',
  `permisison_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `permission_value` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限值',
  `path` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路径',
  `children` bigint(20) NULL DEFAULT 0 COMMENT '子权限',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_permission
-- ----------------------------
INSERT INTO `tb_permission` VALUES (1328619607284133923, '树木管理', '/tree', '/tree', 1328619607284133945, '2020-11-28 15:50:02', '2020-11-28 15:50:04');
INSERT INTO `tb_permission` VALUES (1328619607284133936, 'Logo管理', '/logo/List', '/logo/List', 0, '2020-11-28 15:51:16', '2020-11-28 15:51:19');
INSERT INTO `tb_permission` VALUES (1328619607284133945, '树木列表', '/tree/List', '/tree/List', 0, '2020-11-28 15:53:27', '2020-11-28 15:53:29');
INSERT INTO `tb_permission` VALUES (1328619607284133967, '用户管理', '/user/List', '/user/List', 0, '2020-11-28 15:54:07', '2020-11-28 15:54:10');

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role`  (
  `id` bigint(20) NOT NULL COMMENT '角色Id',
  `role_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `role_value` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色值',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_role
-- ----------------------------
INSERT INTO `tb_role` VALUES (1328619607284133989, '超级管理员', 'admin', '超级管理员');
INSERT INTO `tb_role` VALUES (1328619607284133990, 'user', 'user', '普通用户');

-- ----------------------------
-- Table structure for tb_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_role_permission`;
CREATE TABLE `tb_role_permission`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `permission_id` bigint(20) NOT NULL COMMENT '权限id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_role_permission
-- ----------------------------
INSERT INTO `tb_role_permission` VALUES (1328619607284133111, 1328619607284133989, 1328619607284133923);
INSERT INTO `tb_role_permission` VALUES (1328619607284133112, 1328619607284133989, 1328619607284133945);
INSERT INTO `tb_role_permission` VALUES (1328619607284133113, 1328619607284133989, 1328619607284133936);
INSERT INTO `tb_role_permission` VALUES (1328619607284133114, 1328619607284133989, 1328619607284133967);

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `nick_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像路径',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `phone` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `root` int(2) UNSIGNED NULL DEFAULT 0 COMMENT '超级管理员 1-是 0-否',
  `email` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `login_date` datetime(0) NULL DEFAULT NULL COMMENT '最后一次登录时间',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1328619607284133888, 'root', '超级管理员', 'https://www.yiibai.com/uploads/images/201707/1807/936120749_19056.png', 'e10adc3949ba59abbe56e057f20f883e', '15979807792', 1, '898522599@qq.com', '2020-11-28 09:21:38', '2020-11-18 17:00:02', '2020-11-18 17:00:04');

-- ----------------------------
-- Table structure for tb_user_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_role`;
CREATE TABLE `tb_user_role`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_role
-- ----------------------------
INSERT INTO `tb_user_role` VALUES (1328619607284133232, 1328619607284133888, 1328619607284133990);
INSERT INTO `tb_user_role` VALUES (1328619607284133812, 1328619607284133888, 1328619607284133989);

SET FOREIGN_KEY_CHECKS = 1;
