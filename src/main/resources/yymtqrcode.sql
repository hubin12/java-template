/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50726
 Source Host           : localhost:3306
 Source Schema         : yymtqrcode

 Target Server Type    : MySQL
 Target Server Version : 50726
 File Encoding         : 65001

 Date: 27/11/2020 08:56:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for yymt_permission
-- ----------------------------
DROP TABLE IF EXISTS `yymt_permission`;
CREATE TABLE `yymt_permission`  (
  `id` int(20) NOT NULL COMMENT '主键',
  `permission` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限值',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yymt_permission
-- ----------------------------
INSERT INTO `yymt_permission` VALUES (1, '/tree/List', '古树木管理');
INSERT INTO `yymt_permission` VALUES (2, '/logo/Logo', 'Logo管理');
INSERT INTO `yymt_permission` VALUES (3, '/user/List', '用户管理');

-- ----------------------------
-- Table structure for yymt_tree
-- ----------------------------
DROP TABLE IF EXISTS `yymt_tree`;
CREATE TABLE `yymt_tree`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '古树名木名称',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '百度百科URL',
  `logo_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'logo对应的文件路径',
  `code_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '二维码对应的文件路径',
  `width` int(10) NULL DEFAULT NULL COMMENT '二维码宽度',
  `height` int(10) NULL DEFAULT NULL COMMENT '二维码高度',
  `pinyin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '树木拼音',
  `english` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '树木英文',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uni`(`name`) USING BTREE COMMENT '唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yymt_tree
-- ----------------------------
INSERT INTO `yymt_tree` VALUES (1331802670432399360, '云衫', 'https://baike.baidu.com/item/%E4%BA%91%E6%9D%89', '\\upload\\logo.png', '\\QrCode\\1331802670432399360_3.png', 900, 900, 'yún shān', 'Picea asperata Mast.', '2020-11-26 11:31:15', '2020-11-26 11:31:15');
INSERT INTO `yymt_tree` VALUES (1331803231215038464, '冬青', 'https://baike.baidu.com/item/%E5%86%AC%E9%9D%92/28972', '\\upload\\logo.png', '\\QrCode\\1331803231215038464_3.png', 900, 900, 'dōng qīng', 'Ilex chinensis Sims', '2020-11-26 11:33:29', '2020-11-26 11:33:29');
INSERT INTO `yymt_tree` VALUES (1331803626578522112, '马尾松', 'https://baike.baidu.com/item/%E9%A9%AC%E5%B0%BE%E6%9D%BE', '\\upload\\logo.png', '\\QrCode\\1331803626578522112_3.png', 900, 900, 'mǎ yǐ sōng', 'Pinus massoniana Lamb.', '2020-11-26 11:35:03', '2020-11-26 11:35:03');
INSERT INTO `yymt_tree` VALUES (1331803951850991616, '全缘叶栾树', 'https://baike.baidu.com/item/%E5%85%A8%E7%BC%98%E5%8F%B6%E6%A0%BE%E6%A0%91/4867283', '\\upload\\logo.png', '\\QrCode\\1331803951850991616_3.png', 900, 900, 'quan yuan ye luan shu', 'Koelreuteria bipinnata Franch. var. integrifoliola (Merr.) T. Chen', '2020-11-26 11:36:21', '2020-11-26 11:36:21');

-- ----------------------------
-- Table structure for yymt_user
-- ----------------------------
DROP TABLE IF EXISTS `yymt_user`;
CREATE TABLE `yymt_user`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `nick_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `phone` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `root` int(2) UNSIGNED NULL DEFAULT 0 COMMENT '超级管理员 1-是 0-否',
  `email` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `login_date` datetime(0) NULL DEFAULT NULL COMMENT '最后一次登录时间',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yymt_user
-- ----------------------------
INSERT INTO `yymt_user` VALUES (1328619607284133888, 'root', '超级管理员', 'e10adc3949ba59abbe56e057f20f883e', '15979807792', 1, '898522599@qq.com', '2020-11-26 16:42:11', '2020-11-18 17:00:02', '2020-11-18 17:00:04');
INSERT INTO `yymt_user` VALUES (1331535051724828672, 'user', '普通用户', 'e10adc3949ba59abbe56e057f20f883e', '121545545', 0, '898522299@qq.com', '2020-11-27 08:29:56', '2020-11-25 17:47:50', '2020-11-26 11:00:12');

-- ----------------------------
-- Table structure for yymt_user_permission
-- ----------------------------
DROP TABLE IF EXISTS `yymt_user_permission`;
CREATE TABLE `yymt_user_permission`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `permission_id` bigint(20) NOT NULL COMMENT '权限id',
  PRIMARY KEY (`user_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yymt_user_permission
-- ----------------------------
INSERT INTO `yymt_user_permission` VALUES (1328619607284133888, 1);
INSERT INTO `yymt_user_permission` VALUES (1328619607284133888, 2);
INSERT INTO `yymt_user_permission` VALUES (1328619607284133888, 3);
INSERT INTO `yymt_user_permission` VALUES (1331535051724828672, 1);
INSERT INTO `yymt_user_permission` VALUES (1331535051724828672, 2);

SET FOREIGN_KEY_CHECKS = 1;
