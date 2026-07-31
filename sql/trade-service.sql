/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8.0
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3307
 Source Schema         : trade-service

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 31/07/2026 17:49:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for trade_account_flow
-- ----------------------------
DROP TABLE IF EXISTS `trade_account_flow`;
CREATE TABLE `trade_account_flow`  (
  `id` bigint NOT NULL COMMENT '流水ID',
  `flow_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流水号',
  `account_type` int NOT NULL COMMENT '账户类型 1用户 2商户',
  `account_id` bigint NOT NULL COMMENT '账户ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联支付单号',
  `flow_type` int NOT NULL COMMENT '流水类型',
  `amount` decimal(12, 2) NOT NULL COMMENT '变动金额',
  `before_balance` decimal(12, 2) NULL DEFAULT NULL COMMENT '变更前余额',
  `after_balance` decimal(12, 2) NULL DEFAULT NULL COMMENT '变更后余额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_no`(`flow_no` ASC) USING BTREE,
  INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户资金流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_account_flow
-- ----------------------------

-- ----------------------------
-- Table structure for trade_api_log
-- ----------------------------
DROP TABLE IF EXISTS `trade_api_log`;
CREATE TABLE `trade_api_log`  (
  `id` bigint NOT NULL COMMENT '日志ID',
  `merchant_id` bigint NULL DEFAULT NULL COMMENT '商户ID',
  `api_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应结果',
  `cost_time` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口调用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_api_log
-- ----------------------------

-- ----------------------------
-- Table structure for trade_merchant
-- ----------------------------
DROP TABLE IF EXISTS `trade_merchant`;
CREATE TABLE `trade_merchant`  (
  `id` bigint NOT NULL COMMENT '商户ID',
  `merchant_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户编号',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户名称',
  `merchant_type` int NULL DEFAULT 1 COMMENT '商户类型',
  `app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用Key',
  `app_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用密钥',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_no`(`merchant_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '交易平台商户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_merchant
-- ----------------------------

-- ----------------------------
-- Table structure for trade_merchant_account
-- ----------------------------
DROP TABLE IF EXISTS `trade_merchant_account`;
CREATE TABLE `trade_merchant_account`  (
  `id` bigint NOT NULL COMMENT '账户ID',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `account_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户编号',
  `balance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '余额',
  `frozen_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '冻结金额',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_id`(`merchant_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_account_no`(`account_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户资金账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_merchant_account
-- ----------------------------

-- ----------------------------
-- Table structure for trade_payment_channel
-- ----------------------------
DROP TABLE IF EXISTS `trade_payment_channel`;
CREATE TABLE `trade_payment_channel`  (
  `id` bigint NOT NULL COMMENT '渠道ID',
  `channel_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道编码',
  `channel_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道名称',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_channel_code`(`channel_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付渠道表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_payment_channel
-- ----------------------------
INSERT INTO `trade_payment_channel` VALUES (1, 'BALANCE', '余额支付', 1, '2026-07-31 17:48:45');
INSERT INTO `trade_payment_channel` VALUES (2, 'ALIPAY', '支付宝', 1, '2026-07-31 17:48:45');
INSERT INTO `trade_payment_channel` VALUES (3, 'WECHAT', '微信支付', 1, '2026-07-31 17:48:45');

-- ----------------------------
-- Table structure for trade_payment_notify
-- ----------------------------
DROP TABLE IF EXISTS `trade_payment_notify`;
CREATE TABLE `trade_payment_notify`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `notify_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知地址',
  `request_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应结果',
  `notify_status` int NULL DEFAULT 0 COMMENT '通知状态',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付回调通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_payment_notify
-- ----------------------------

-- ----------------------------
-- Table structure for trade_payment_order
-- ----------------------------
DROP TABLE IF EXISTS `trade_payment_order`;
CREATE TABLE `trade_payment_order`  (
  `id` bigint NOT NULL COMMENT '支付订单ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付流水号',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务订单号',
  `user_id` bigint NOT NULL COMMENT '付款用户',
  `amount` decimal(12, 2) NOT NULL COMMENT '支付金额',
  `payment_type` int NULL DEFAULT 1 COMMENT '支付类型',
  `status` int NULL DEFAULT 0 COMMENT '支付状态',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '支付过期时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_payment_order
-- ----------------------------

-- ----------------------------
-- Table structure for trade_recharge_order
-- ----------------------------
DROP TABLE IF EXISTS `trade_recharge_order`;
CREATE TABLE `trade_recharge_order`  (
  `id` bigint NOT NULL COMMENT '充值订单ID',
  `recharge_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '充值单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(12, 2) NOT NULL COMMENT '充值金额',
  `status` int NULL DEFAULT 0 COMMENT '状态',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_recharge_no`(`recharge_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户充值订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_recharge_order
-- ----------------------------

-- ----------------------------
-- Table structure for trade_refund_order
-- ----------------------------
DROP TABLE IF EXISTS `trade_refund_order`;
CREATE TABLE `trade_refund_order`  (
  `id` bigint NOT NULL COMMENT '退款ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款单号',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `refund_amount` decimal(12, 2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `status` int NULL DEFAULT 0 COMMENT '退款状态',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_refund_no`(`refund_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_refund_order
-- ----------------------------

-- ----------------------------
-- Table structure for trade_user_account
-- ----------------------------
DROP TABLE IF EXISTS `trade_user_account`;
CREATE TABLE `trade_user_account`  (
  `id` bigint NOT NULL COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '平台用户ID',
  `account_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户编号',
  `balance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `frozen_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '冻结金额',
  `status` int NULL DEFAULT 1 COMMENT '账户状态 1正常 0冻结',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_account_no`(`account_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_user_account
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
