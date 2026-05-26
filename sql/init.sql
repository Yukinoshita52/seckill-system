-- sql/init.sql

CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seckill;

-- 秒杀活动表
CREATE TABLE t_seckill_activity (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_name   VARCHAR(100) NOT NULL COMMENT '活动名称',
    goods_id        BIGINT NOT NULL COMMENT '商品ID',
    goods_name      VARCHAR(200) NOT NULL COMMENT '商品名称',
    original_price  DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price   DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    total_stock     INT NOT NULL COMMENT '总库存',
    bucket_count    INT NOT NULL DEFAULT 5 COMMENT '分桶数量',
    start_time      DATETIME NOT NULL COMMENT '开始时间',
    end_time        DATETIME NOT NULL COMMENT '结束时间',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已结束',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-已删除',
    INDEX idx_status_time (status, start_time, end_time)
) COMMENT '秒杀活动表';

-- 秒杀订单表
CREATE TABLE t_seckill_order (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no        VARCHAR(64) NOT NULL COMMENT '订单号',
    activity_id     BIGINT NOT NULL COMMENT '活动ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    seckill_price   DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    bucket_index    INT NOT NULL COMMENT '分桶索引',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已取消 3-已超时',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pay_time        DATETIME COMMENT '支付时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-已删除',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_user_activity (user_id, activity_id),
    INDEX idx_status (status, create_time)
) COMMENT '秒杀订单表';

-- 库存扣减流水表
CREATE TABLE t_stock_deduct_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id     BIGINT NOT NULL COMMENT '活动ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    bucket_index    INT NOT NULL COMMENT '分桶索引',
    deduct_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '扣减时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-已删除',
    INDEX idx_activity (activity_id, deduct_time)
) COMMENT '库存扣减流水表';

-- 用户表
CREATE TABLE t_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(128) NOT NULL COMMENT 'BCrypt加密',
    nickname        VARCHAR(50),
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-已删除'
) COMMENT '用户表';

-- 初始测试用户（密码均为 123456）
INSERT INTO t_user (username, password, nickname) VALUES
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户1'),
('user002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户2'),
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员');
