package com.seckill.common.constant;

/** Redis key 前缀常量，统一管理 key 格式 */
public final class RedisKeyConstants {

  private RedisKeyConstants() {}

  /** activity:{activityId} — 活动信息 Hash */
  public static final String ACTIVITY_PREFIX = "activity:";

  /** stock:{activityId}:{bucket} — 分桶库存 */
  public static final String STOCK_PREFIX = "stock:";

  /** total:{activityId} — 总库存计数 */
  public static final String TOTAL_PREFIX = "total:";

  /** activity:ids — 全量活动 ID 集合 */
  public static final String ACTIVITY_IDS_SET = "activity:ids";

  /** bought:{activityId} — 已购买用户 Set */
  public static final String BOUGHT_PREFIX = "bought:";

  public static String activityKey(Long activityId) {
    return ACTIVITY_PREFIX + activityId;
  }

  public static String stockKey(Long activityId, int bucket) {
    return STOCK_PREFIX + activityId + ":" + bucket;
  }

  public static String totalKey(Long activityId) {
    return TOTAL_PREFIX + activityId;
  }

  public static String boughtKey(Long activityId) {
    return BOUGHT_PREFIX + activityId;
  }
}
