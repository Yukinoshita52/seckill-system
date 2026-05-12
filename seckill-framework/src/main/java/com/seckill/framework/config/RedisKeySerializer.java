package com.seckill.framework.config;

import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisKeySerializer extends StringRedisSerializer {
  private final String prefix;

  public RedisKeySerializer(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String deserialize(byte[] bytes) {
    String key = super.deserialize(bytes);
    if (key != null && key.startsWith(prefix)) {
      return key.substring(prefix.length());
    }
    return key;
  }

  @Override
  public byte[] serialize(String key) {
    return super.serialize(prefix + key);
  }
}
