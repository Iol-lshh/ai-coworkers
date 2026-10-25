package ai.coworkers.core.common;

import java.security.SecureRandom;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 생성 시각 순서로 정렬되는 버전 7 UUID */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UuidV7 {

  private static final int VERSION = 7;
  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * 현재 시각으로 새 UUID를 생성함.
   *
   * @return 생성한 UUID
   */
  public static UUID generate() {
    long epochMillis = System.currentTimeMillis();
    long mostSigBits = (epochMillis << 16) | (VERSION << 12) | (RANDOM.nextLong() & 0x0FFFL);
    long leastSigBits = (RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL) | Long.MIN_VALUE;
    return new UUID(mostSigBits, leastSigBits);
  }
}
