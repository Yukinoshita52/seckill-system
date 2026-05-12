package com.seckill.framework.toolkit;

public final class StockDecrementReturnCombinedUtil {
  private static final int COUNT_BITS = 14;
  private static final long COUNT_MASK = (1L << COUNT_BITS) - 1;

  public static int parseErrorCode(long combined) {
    return (int) (combined >> COUNT_BITS);
  }

  public static long parseCount(long combined) {
    return combined & COUNT_MASK;
  }

  public static long combine(int errorCode, long count) {
    return ((long) errorCode << COUNT_BITS) | (count & COUNT_MASK);
  }
}
