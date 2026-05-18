-- 修复所有表注释乱码（连接编码问题导致 COMMENT 存为 latin1）
-- 执行前请确保连接使用 utf8mb4: SET NAMES utf8mb4;

-- 1. t_seckill_activity
ALTER TABLE `t_seckill_activity`
  MODIFY `activity_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  MODIFY `goods_id` bigint NOT NULL COMMENT '商品ID',
  MODIFY `goods_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  MODIFY `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  MODIFY `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  MODIFY `total_stock` int NOT NULL COMMENT '总库存',
  MODIFY `bucket_count` int NOT NULL DEFAULT '5' COMMENT '分桶数量',
  MODIFY `start_time` datetime NOT NULL COMMENT '开始时间',
  MODIFY `end_time` datetime NOT NULL COMMENT '结束时间',
  MODIFY `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-未开始 1-进行中 2-已结束',
  MODIFY `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-正常 1-已删除',
  COMMENT='秒杀活动表';

-- 2. t_seckill_order
ALTER TABLE `t_seckill_order`
  MODIFY `order_no` varchar(64) NOT NULL COMMENT '订单号',
  MODIFY `activity_id` bigint NOT NULL COMMENT '活动ID',
  MODIFY `user_id` bigint NOT NULL COMMENT '用户ID',
  MODIFY `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  MODIFY `bucket_index` int NOT NULL COMMENT '分桶索引',
  MODIFY `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-PENDING 1-UNPAID 2-SUCCESS 3-FAILED 4-TIMEOUT',
  MODIFY `pay_time` datetime COMMENT '支付时间',
  MODIFY `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-正常 1-已删除',
  COMMENT='秒杀订单表';

-- 3. t_stock_deduct_log
ALTER TABLE `t_stock_deduct_log`
  MODIFY `activity_id` bigint NOT NULL COMMENT '活动ID',
  MODIFY `user_id` bigint NOT NULL COMMENT '用户ID',
  MODIFY `bucket_index` int NOT NULL COMMENT '分桶索引',
  MODIFY `deduct_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '扣减时间',
  MODIFY `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-正常 1-已删除',
  COMMENT='库存扣减流水';

-- 4. t_user
ALTER TABLE `t_user`
  MODIFY `username` varchar(50) NOT NULL COMMENT '用户名',
  MODIFY `password` varchar(128) NOT NULL COMMENT '密码(BCrypt)',
  MODIFY `nickname` varchar(50) COMMENT '昵称',
  MODIFY `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0-正常 1-已删除',
  COMMENT='用户表';
