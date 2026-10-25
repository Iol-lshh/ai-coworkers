package ai.coworkers.core.model.template.step.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TaskTypeRegistry")
class TaskTypeRegistryTest {

  private static final TaskType ANALYZE = new TestTaskType("test.analyze");
  private static final TaskType REVIEW = new TestTaskType("test.review");

  private record TestTaskType(String code) implements TaskType {
    @Override
    public String name() {
      return code;
    }
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("코드가 중복되면 거부함")
    void rejectsDuplicateCode() {
      TaskType duplicate = new TestTaskType("test.analyze");

      assertThatThrownBy(() -> new TaskTypeRegistry(List.of(ANALYZE, duplicate)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate task type code: test.analyze");
    }

    @ParameterizedTest(name = "code [{0}]")
    @EmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("코드가 비어 있으면 거부함")
    void rejectsBlankCode(String code) {
      assertThatThrownBy(() -> new TaskTypeRegistry(List.of(new TestTaskType(code))))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("task type code must not be blank");
    }
  }

  @Nested
  @DisplayName("find")
  class Find {

    private final TaskTypeRegistry registry = new TaskTypeRegistry(List.of(ANALYZE, REVIEW));

    @Test
    @DisplayName("등록한 코드로 작업 종류를 찾음")
    void findsRegisteredType() {
      assertThat(registry.find("test.review")).isSameAs(REVIEW);
    }

    @Test
    @DisplayName("등록되지 않은 코드면 거부함")
    void rejectsUnknownCode() {
      assertThatThrownBy(() -> registry.find("test.unknown"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown task type code: test.unknown");
    }
  }
}
