-- =====================================================
-- Seata AT 模式 Demo - 数据库初始化脚本
-- =====================================================

-- 1. 创建订单数据库
CREATE DATABASE IF NOT EXISTS seata_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata_order;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `count` INT NOT NULL COMMENT '购买数量',
    `money` DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    `status` INT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-初始化, 1-已完成',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- Seata AT 模式必须的 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT NOT NULL COMMENT '状态: 0-正常, 1-已防御',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata undo_log 表';


-- 2. 创建库存数据库
CREATE DATABASE IF NOT EXISTS seata_storage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata_storage;

-- 库存表
CREATE TABLE IF NOT EXISTS `storage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `total` INT NOT NULL COMMENT '总库存',
    `used` INT NOT NULL DEFAULT 0 COMMENT '已使用',
    `residue` INT NOT NULL DEFAULT 0 COMMENT '剩余',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 初始化库存数据
INSERT INTO `storage` (`product_id`, `total`, `used`, `residue`) VALUES (1, 100, 0, 100) ON DUPLICATE KEY UPDATE `product_id`=`product_id`;

-- Seata AT 模式必须的 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT NOT NULL COMMENT '状态: 0-正常, 1-已防御',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata undo_log 表';


-- 3. 创建账户数据库
CREATE DATABASE IF NOT EXISTS seata_account DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata_account;

-- 账户表
CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total` DECIMAL(10, 2) NOT NULL COMMENT '总额度',
    `used` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '已使用',
    `residue` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '剩余',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- 初始化账户数据
INSERT INTO `account` (`user_id`, `total`, `used`, `residue`) VALUES (1, 1000.00, 0, 1000.00) ON DUPLICATE KEY UPDATE `user_id`=`user_id`;

-- Seata AT 模式必须的 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT NOT NULL COMMENT '状态: 0-正常, 1-已防御',
    `log_created` DATETIME NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata undo_log 表';
