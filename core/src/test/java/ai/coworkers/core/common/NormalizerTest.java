package ai.coworkers.core.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Normalizer")
class NormalizerTest {

  private static final Normalizer<String> STRIP = String::strip;
  private static final Normalizer<String> LOWER = String::toLowerCase;

  @Nested
  @DisplayName("normalize")
  class Normalize {

    @ParameterizedTest(name = "[{0}] → [{1}]")
    @CsvSource({
      "'  a  ', a",
      "a, a",
      "'', ''",
    })
    @DisplayName("구현한 연산 결과를 반환함")
    void returnsNormalized(String target, String expected) {
      assertThat(STRIP.normalize(target)).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("from")
  class From {

    @ParameterizedTest(name = "[{0}] → [{1}]")
    @CsvSource({
      "ABC, abc",
      "abc, abc",
      "'', ''",
    })
    @DisplayName("UnaryOperator에 위임하는 정규화 정책을 생성함")
    void delegatesToOperator(String target, String expected) {
      Normalizer<String> normalizer = Normalizer.from(String::toLowerCase);

      assertThat(normalizer.normalize(target)).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("apply")
  class Apply {

    @ParameterizedTest(name = "[{0}]")
    @CsvSource({
      "'  a  '", "a", "''",
    })
    @DisplayName("normalize 결과와 같은 값을 반환함")
    void delegatesToNormalize(String target) {
      assertThat(STRIP.apply(target)).isEqualTo(STRIP.normalize(target));
    }
  }

  @Nested
  @DisplayName("then")
  class Then {

    @ParameterizedTest(name = "[{0}] → [{1}]")
    @CsvSource({
      "'  AbC  ', abc",
      "ABC, abc",
      "'', ''",
    })
    @DisplayName("두 정책을 모두 적용함")
    void appliesBoth(String target, String expected) {
      assertThat(STRIP.then(LOWER).normalize(target)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{0}] → [{1}]")
    @CsvSource({
      "x, xab", "'', ab",
    })
    @DisplayName("이 정책을 먼저 적용한 뒤 next를 적용함")
    void appliesInOrder(String target, String expected) {
      Normalizer<String> appendA = value -> value + "a";
      Normalizer<String> appendB = value -> value + "b";

      assertThat(appendA.then(appendB).normalize(target)).isEqualTo(expected);
    }
  }
}
