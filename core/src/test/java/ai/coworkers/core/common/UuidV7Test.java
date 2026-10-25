package ai.coworkers.core.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UuidV7")
class UuidV7Test {

  @Nested
  @DisplayName("generate")
  class Generate {

    @Test
    @DisplayName("버전 7과 variant 2를 가진 UUID를 생성함")
    void generatesVersion7() {
      UUID uuid = UuidV7.generate();

      assertThat(uuid).extracting(UUID::version, UUID::variant).containsExactly(7, 2);
    }

    @Test
    @DisplayName("생성 시각을 앞 48비트에 담음")
    void embedsCreationTime() {
      long before = System.currentTimeMillis();
      UUID uuid = UuidV7.generate();
      long after = System.currentTimeMillis();

      assertThat(uuid.getMostSignificantBits() >>> 16).isBetween(before, after);
    }

    @Test
    @DisplayName("나중에 생성한 값이 문자열 순서에서 뒤에 옴")
    void ordersByCreationTime() throws InterruptedException {
      String first = UuidV7.generate().toString();
      Thread.sleep(2);
      String second = UuidV7.generate().toString();

      assertThat(first).isLessThan(second);
    }

    @Test
    @DisplayName("매번 다른 값을 생성함")
    void generatesUniqueValues() {
      assertThat(Stream.generate(UuidV7::generate).limit(1_000)).doesNotHaveDuplicates();
    }
  }
}
