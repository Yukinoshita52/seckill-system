-- KEYS[1] = 分桶库存 Key, KEYS[2] = 用户去重 Set Key, KEYS[3] = 总库存 Key
-- ARGV[1] = userId

redis.call('INCR', KEYS[1])
redis.call('INCR', KEYS[3])
redis.call('SREM', KEYS[2], ARGV[1])
return 1
