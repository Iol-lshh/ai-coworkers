package ai.coworkers.core.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("PageSpecifications")
class PageSpecificationsTest {

  private record TestPageCommand(long page, long size) implements PageCommand {}

  @Nested
  @DisplayName("VALID")
  class Valid {

    @ParameterizedTest(name = "page={0}, size={1}")
    @CsvSource({"0, 1", "0, 100", "5, 1"})
    @DisplayName("페이지 번호가 0 이상이고 크기가 1 이상이면 만족함")
    void satisfiedByValidPage(long page, long size) {
      assertThat(PageSpecifications.VALID.isSatisfiedBy(new TestPageCommand(page, size))).isTrue();
    }

    @ParameterizedTest(name = "page={0}, size={1}")
    @CsvSource({"-1, 10", "0, 0", "0, -1"})
    @DisplayName("페이지 번호가 음수이거나 크기가 1보다 작으면 거부함")
    void rejectsInvalidPage(long page, long size) {
      assertThatThrownBy(
              () -> PageSpecifications.VALID.isSatisfiedBy(new TestPageCommand(page, size)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("invalid page: page=" + page + ", size=" + size);
    }
  }
}
