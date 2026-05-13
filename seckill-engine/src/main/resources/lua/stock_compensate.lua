-- KEYS[1] = 分桶库存 Key, KEYS[2] = 用户去重 Set Key
-- ARGV[1] = userId

-- todo: 添加 SISMEMBER 校验，确认用户确实在 bought set 中才执行回补，防止库存膨胀
redis.call('INCR', KEYS[1])
redis.call('SREM', KEYS[2], ARGV[1])
return 1
