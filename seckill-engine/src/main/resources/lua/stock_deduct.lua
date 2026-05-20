-- KEYS[1] = 分桶库存 Key, KEYS[2] = 用户去重 Set Key, KEYS[3] = 总库存 Key
-- ARGV[1] = userId
-- 返回: (errorCode << 14) | remainingStock
-- errorCode: 0=成功, 1=库存不足, 2=已购买过

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return bit.bor(bit.lshift(2, 14), 0)
end

local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    -- 当前桶库存耗尽，但总库存可能仍有剩余（未实现跨桶查找策略）
    local total = tonumber(redis.call('GET', KEYS[3])) or 0
    return bit.bor(bit.lshift(1, 14), total)
end

redis.call('DECR', KEYS[1])
redis.call('DECR', KEYS[3])
redis.call('SADD', KEYS[2], ARGV[1])
local remaining = stock - 1
return bit.bor(bit.lshift(0, 14), remaining)
