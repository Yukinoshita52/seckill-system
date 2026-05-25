-- t_seckill_activity.status 字段含义变更
-- 0-未开始 1-进行中 2-已售罄 3-已结束（原 2-已结束）
ALTER TABLE t_seckill_activity MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '状态: 0-未开始 1-进行中 2-已售罄 3-已结束';
