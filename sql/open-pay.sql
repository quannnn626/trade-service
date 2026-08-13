/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8.0
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3307
 Source Schema         : open-pay

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 12/08/2026 10:14:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for auth_user
-- ----------------------------
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE `auth_user`  (
  `id` bigint NOT NULL COMMENT '用户ID',
  `user_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户编号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '加密密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '展示昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` int NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_no`(`user_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '认证用户表（归属 auth 体系）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auth_user
-- ----------------------------
INSERT INTO `auth_user` VALUES (1, 'U20240001', 'admin', '$2a$10$fqNtq/X0WFtUS8mkAWy5z.ZIwKpFLUWdu0PjtvxPLGNxV04KL0euy', '管理员', '', '13800000000', 'admin@test.com', NULL, 1, '2026-08-04 17:00:00', '2026-08-04 17:00:00');
INSERT INTO `auth_user` VALUES (2084908504743415809, 'UR2084908504455385088', 'test1', '$2a$10$OE.D06ZjNHxYdcE8Ws/0aedkoMEpyM8jKzZ5NkfpEJ50cNss2ods6', 'test1', '', NULL, NULL, NULL, 1, '2026-08-05 15:44:57', '2026-08-05 15:44:57');

-- ----------------------------
-- Table structure for pay_account_flow
-- ----------------------------
DROP TABLE IF EXISTS `pay_account_flow`;
CREATE TABLE `pay_account_flow`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户资金流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_account_flow
-- ----------------------------

-- ----------------------------
-- Table structure for pay_api_log
-- ----------------------------
DROP TABLE IF EXISTS `pay_api_log`;
CREATE TABLE `pay_api_log`  (
  `id` bigint NOT NULL COMMENT '日志ID',
  `merchant_id` bigint NULL DEFAULT NULL COMMENT '商户ID',
  `api_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `request_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应结果',
  `cost_time` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `merchant_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商户编号',
  `sign_result` int NULL DEFAULT 0 COMMENT '验签结果 0-通过 1-失败',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求UA',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口调用日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_api_log
-- ----------------------------
INSERT INTO `pay_api_log` VALUES (2087358211910606849, 2085297007843151873, 'pay.create', 'POST', '/open/pay/create', '[serialize error]', '[exception: DataIntegrityViolationException]', 133, '2026-08-12 09:59:13', 'M20260806580286976', 0, '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'user_id\' doesn\'t have a default value\r\n### The error may exist in com/boot/pay/mapper/PayPaymentOrderMapper.java (best guess)\r\n### The error may involve com.boot.pay.mapper.PayPaymentOrderMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO pay_payment_order  ( id, payment_no, merchant_id, order_no,  amount, status, expire_time,    merchant_payment_no, channel_id, client_ip, subject,     t', 'PostmanRuntime/7.51.1');
INSERT INTO `pay_api_log` VALUES (2087359532688547843, 2085297007843151873, 'pay.create', 'POST', '/open/pay/create', '[serialize error]', '[exception: DataIntegrityViolationException]', 17, '2026-08-12 10:04:28', 'M20260806580286976', 0, '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'user_id\' doesn\'t have a default value\r\n### The error may exist in com/boot/pay/mapper/PayPaymentOrderMapper.java (best guess)\r\n### The error may involve com.boot.pay.mapper.PayPaymentOrderMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO pay_payment_order  ( id, payment_no, merchant_id, order_no,  amount, status, expire_time,    merchant_payment_no, channel_id, client_ip, subject,     t', 'PostmanRuntime/7.51.1');
INSERT INTO `pay_api_log` VALUES (2087359624233426947, 2085297007843151873, 'pay.create', 'POST', '/open/pay/create', '[serialize error]', '[exception: DataIntegrityViolationException]', 12, '2026-08-12 10:04:50', 'M20260806580286976', 0, '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'user_id\' doesn\'t have a default value\r\n### The error may exist in com/boot/pay/mapper/PayPaymentOrderMapper.java (best guess)\r\n### The error may involve com.boot.pay.mapper.PayPaymentOrderMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO pay_payment_order  ( id, payment_no, merchant_id, order_no,  amount, status, expire_time,    merchant_payment_no, channel_id, client_ip, subject,     t', 'PostmanRuntime/7.51.1');
INSERT INTO `pay_api_log` VALUES (2087359887149178882, 2085297007843151873, 'pay.create', 'POST', '/open/pay/create', '[serialize error]', '{\"code\":0,\"data\":{\"amount\":99.00,\"expireTime\":\"2026-08-12 10:35:53.027\",\"paymentNo\":\"PAY20260812158939648\",\"status\":0,\"statusDesc\":\"待支付\"},\"message\":\"success\"}', 19, '2026-08-12 10:05:53', 'M20260806580286976', 0, NULL, 'PostmanRuntime/7.51.1');

-- ----------------------------
-- Table structure for pay_merchant
-- ----------------------------
DROP TABLE IF EXISTS `pay_merchant`;
CREATE TABLE `pay_merchant`  (
  `id` bigint NOT NULL COMMENT '商户ID',
  `merchant_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户编号',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户名称',
  `merchant_type` int NULL DEFAULT 1 COMMENT '商户类型',
  `app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用Key',
  `app_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用密钥',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notify_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认回调通知地址',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `company_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业全称',
  `business_license` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `settle_type` int NULL DEFAULT 1 COMMENT '结算方式 1-T+1 2-T+0 3-周结 4-月结',
  `settle_fee_rate` decimal(5, 4) NULL DEFAULT 0.0060 COMMENT '结算费率（如0.0060=0.6%）',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '授权过期时间',
  `secret_version` int NULL DEFAULT 1 COMMENT '密钥版本号',
  `white_ip_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'IP白名单（JSON数组）',
  `daily_limit` decimal(12, 2) NULL DEFAULT NULL COMMENT '单日交易限额',
  `single_limit` decimal(12, 2) NULL DEFAULT NULL COMMENT '单笔交易限额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `audit_status` int NULL DEFAULT 0 COMMENT '审核状态 0-待审核 1-已通过 2-已驳回',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_no`(`merchant_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '交易平台商户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_merchant
-- ----------------------------
INSERT INTO `pay_merchant` VALUES (2085297007843151873, 'M20260806580286976', '测试商户', 1, '77c6afca6c6b991d', '006d20745435432a838daad7fc19bfd8', 1, '2026-08-06 17:28:44', '2026-08-10 13:54:14', NULL, '张三', '13800000000', 'zhangsan@test.com', 'XX科技有限公司', '91110000123456789X', 1, 0.0060, NULL, 2, NULL, 100000.00, 10000.00, NULL, 1, '企业资质审核通过');
INSERT INTO `pay_merchant` VALUES (2085557096843902977, 'M20260807815923200', '测试商户2', 1, '5345aa160624a9f9', 'fa8900a615504d21ae36bd5e7a653b76', 0, '2026-08-07 10:42:14', '2026-08-07 10:42:14', NULL, '张三', '13800000001', 'zhangsan@test.com', 'XX科技有限公司', '91110000123456789X', 1, 0.0060, NULL, 1, NULL, 100000.00, 10000.00, NULL, 2, '营业执照号与公司名称不一致，请核实');
INSERT INTO `pay_merchant` VALUES (2085617616783491074, 'M20260807738717696', '测试商户3', 1, 'cc97a3decba03f65', '44df316bca274ff9bbd5ad1fe31ef9c5', 0, '2026-08-07 14:42:43', '2026-08-07 14:42:43', NULL, '张三', '13800000002', 'zhangsan@test.com', 'XX科技有限公司', '91110000123456789X', 1, 0.0060, NULL, 1, NULL, 100000.00, 10000.00, NULL, 2, '营业执照号与公司名称不一致，请核实');
INSERT INTO `pay_merchant` VALUES (2085617758337056770, 'M20260807363586560', '测试商户3', 1, '073b6f87478dbf50', '45f7a0861a074707a809c2fd54ad8a49', 1, '2026-08-07 14:43:17', '2026-08-07 14:43:17', NULL, '张三', '13800000002', 'zhangsan@test.com', 'XX科技有限公司', '91110000123456789X', 1, 0.0060, NULL, 1, NULL, 100000.00, 10000.00, NULL, 1, '企业资质审核通过');

-- ----------------------------
-- Table structure for pay_merchant_account
-- ----------------------------
DROP TABLE IF EXISTS `pay_merchant_account`;
CREATE TABLE `pay_merchant_account`  (
  `id` bigint NOT NULL COMMENT '账户ID',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `account_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户编号',
  `balance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '余额',
  `frozen_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '冻结金额',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_id`(`merchant_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_account_no`(`account_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户资金账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_merchant_account
-- ----------------------------
INSERT INTO `pay_merchant_account` VALUES (2085555584205914113, 2085297007843151873, 'MA20260807194711552', 0.00, 0.00, 1, '2026-08-07 10:36:13', '2026-08-07 10:36:13', 0);
INSERT INTO `pay_merchant_account` VALUES (2085618404968710146, 2085617758337056770, 'MA20260807970074112', 0.00, 0.00, 1, '2026-08-07 14:45:51', '2026-08-07 14:45:51', 0);

-- ----------------------------
-- Table structure for pay_merchant_secret_history
-- ----------------------------
DROP TABLE IF EXISTS `pay_merchant_secret_history`;
CREATE TABLE `pay_merchant_secret_history`  (
  `id` bigint NOT NULL COMMENT '主键',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '历史密钥',
  `version` int NOT NULL COMMENT '密钥版本号',
  `expire_time` datetime NOT NULL COMMENT '失效时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商户密钥历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_merchant_secret_history
-- ----------------------------
INSERT INTO `pay_merchant_secret_history` VALUES (2086692577598074881, 2085297007843151873, 'f0e33d82dc45488eb55d00e0fe142641', 1, '2026-08-11 13:54:14', '2026-08-10 13:54:14');

-- ----------------------------
-- Table structure for pay_payment_channel
-- ----------------------------
DROP TABLE IF EXISTS `pay_payment_channel`;
CREATE TABLE `pay_payment_channel`  (
  `id` bigint NOT NULL COMMENT '渠道ID',
  `channel_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道编码',
  `channel_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道名称',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_channel_code`(`channel_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付渠道表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_payment_channel
-- ----------------------------
INSERT INTO `pay_payment_channel` VALUES (1, 'BALANCE', '余额支付', 1, '2026-07-31 17:48:45');
INSERT INTO `pay_payment_channel` VALUES (2, 'ALIPAY', '支付宝', 1, '2026-07-31 17:48:45');
INSERT INTO `pay_payment_channel` VALUES (3, 'WECHAT', '微信支付', 1, '2026-07-31 17:48:45');

-- ----------------------------
-- Table structure for pay_payment_notify
-- ----------------------------
DROP TABLE IF EXISTS `pay_payment_notify`;
CREATE TABLE `pay_payment_notify`  (
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
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下次重试时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE,
  INDEX `idx_next_retry`(`next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付回调通知记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_payment_notify
-- ----------------------------

-- ----------------------------
-- Table structure for pay_payment_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_payment_order`;
CREATE TABLE `pay_payment_order`  (
  `id` bigint NOT NULL COMMENT '支付订单ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付流水号',
  `merchant_id` bigint NOT NULL COMMENT '商户ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务订单号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '付款用户',
  `amount` decimal(12, 2) NOT NULL COMMENT '支付金额',
  `status` int NULL DEFAULT 0 COMMENT '支付状态',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '支付过期时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `merchant_payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商城支付单号',
  `channel_id` bigint NULL DEFAULT NULL COMMENT '支付渠道ID',
  `client_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端/服务器IP',
  `subject` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品标题（展示用）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单描述',
  `notify_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单级别回调地址',
  `return_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付完成跳转地址',
  `attach` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附加数据（透传，原样返回）',
  `timeout_expire` datetime NULL DEFAULT NULL COMMENT '订单超时自动关闭时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关单时间',
  `close_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关单原因',
  `fee_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '手续费金额',
  `settle_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '结算金额（=amount - fee_amount）',
  `settle_status` int NULL DEFAULT 0 COMMENT '结算状态 0-未结算 1-已结算',
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `sign` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单签名（防篡改）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_merchant_payment`(`merchant_id` ASC, `merchant_payment_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_channel_id`(`channel_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_payment_order
-- ----------------------------
INSERT INTO `pay_payment_order` VALUES (2087359887149178881, 'PAY20260812158939648', 2085297007843151873, 'ORD1786500341434', 0, 99.00, 0, '2026-08-12 10:35:53', NULL, '2026-08-12 10:05:53', '2026-08-12 10:05:53', 'ORD1786500341434', 1, '0:0:0:0:0:0:0:1', '测试商品', NULL, NULL, NULL, NULL, '2026-08-12 10:35:53', NULL, NULL, 0.59, 98.41, 0, NULL, NULL);

-- ----------------------------
-- Table structure for pay_recharge_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_recharge_order`;
CREATE TABLE `pay_recharge_order`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户充值订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_recharge_order
-- ----------------------------

-- ----------------------------
-- Table structure for pay_refund_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_refund_order`;
CREATE TABLE `pay_refund_order`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_refund_order
-- ----------------------------

-- ----------------------------
-- Table structure for pay_user_account
-- ----------------------------
DROP TABLE IF EXISTS `pay_user_account`;
CREATE TABLE `pay_user_account`  (
  `id` bigint NOT NULL COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '平台用户ID',
  `account_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户编号',
  `balance` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `frozen_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '冻结金额',
  `status` int NULL DEFAULT 1 COMMENT '账户状态 1正常 0冻结',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mall_user_id` bigint NULL DEFAULT NULL COMMENT '关联商城用户ID',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `total_income` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '累计收入',
  `total_expense` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '累计支出',
  `pay_password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付密码（BCrypt加密）',
  `real_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实名认证姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实名认证身份证号',
  `real_name_auth` int NULL DEFAULT 0 COMMENT '实名认证状态 0-未认证 1-已认证',
  `daily_limit` decimal(12, 2) NULL DEFAULT 1000.00 COMMENT '单日支付限额（未实名1000，实名后50000）',
  `daily_used` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '今日已支付金额',
  `daily_date` date NULL DEFAULT NULL COMMENT '限额日期（用于重置日限额）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_account_no`(`account_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pay_user_account
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
