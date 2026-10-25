package ai.coworkers.core.model.workflow.step.condition;

import static ai.coworkers.core.common.ArgumentSpecifications.NOT_BLANK;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 말단 조건 설정값을 읽는 규칙 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConditionConfig {

  /** 판정할 값의 이름을 담는 설정 이름 */
  static final String KEY = "key";

  /**
   * 설정값을 읽음. 없거나 비어 있으면 거부함.
   *
   * @param config 설정값
   * @param name 설정 이름
   * @return 설정값
   */
  static String require(Map<String, String> config, String name) {
    String value = config.get(name);
    if (value == null || !NOT_BLANK.isSatisfiedBy(value)) {
      throw new IllegalArgumentException("missing condition config: " + name);
    }
    return value;
  }

  /**
   * 설정값을 논리값으로 읽음. {@code true}, {@code false}가 아니면 거부함.
   *
   * @param config 설정값
   * @param name 설정 이름
   * @return 논리값
   */
  static boolean requireBoolean(Map<String, String> config, String name) {
    String value = require(config, name);
    return switch (value) {
      case "true" -> true;
      case "false" -> false;
      default -> throw invalid(name, value);
    };
  }

  /**
   * 설정값을 숫자로 읽음. 숫자가 아니면 거부함.
   *
   * @param config 설정값
   * @param name 설정 이름
   * @return 숫자
   */
  static BigDecimal requireNumber(Map<String, String> config, String name) {
    String value = require(config, name);
    try {
      return new BigDecimal(value);
    } catch (NumberFormatException e) {
      throw invalid(name, value);
    }
  }

  /**
   * 설정값을 열거 상수로 읽음. 상수 이름이 아니면 거부함.
   *
   * @param config 설정값
   * @param name 설정 이름
   * @param type 열거 타입
   * @param <E> 열거 타입
   * @return 열거 상수
   */
  static <E extends Enum<E>> E requireEnum(Map<String, String> config, String name, Class<E> type) {
    String value = require(config, name);
    try {
      return Enum.valueOf(type, value);
    } catch (IllegalArgumentException e) {
      throw invalid(name, value);
    }
  }

  private static IllegalArgumentException invalid(String name, String value) {
    return new IllegalArgumentException("invalid condition config: " + name + "=" + value);
  }
}
