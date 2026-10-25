package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StepConditionTypeRegistry")
class StepConditionTypeRegistryTest {

  private static final StepConditionType ALWAYS = new FixedStepConditionType("test.always", true);
  private static final StepConditionType NEVER = new FixedStepConditionType("test.never", false);

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("코드가 중복되면 거부함")
    void rejectsDuplicateCode() {
      StepConditionType duplicate = new FixedStepConditionType("test.always", false);

      assertThatThrownBy(() -> new StepConditionTypeRegistry(List.of(ALWAYS, duplicate)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate condition type code: test.always");
    }

    @ParameterizedTest(name = "code [{0}]")
    @EmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("코드가 비어 있으면 거부함")
    void rejectsBlankCode(String code) {
      assertThatThrownBy(
              () -> new StepConditionTypeRegistry(List.of(new FixedStepConditionType(code, true))))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("condition type code must not be blank");
    }
  }

  @Nested
  @DisplayName("find")
  class Find {

    private final StepConditionTypeRegistry registry =
        new StepConditionTypeRegistry(List.of(ALWAYS, NEVER));

    @Test
    @DisplayName("등록한 코드로 조건 종류를 찾음")
    void findsRegisteredType() {
      assertThat(registry.find("test.never")).isSameAs(NEVER);
    }

    @Test
    @DisplayName("등록되지 않은 코드면 거부함")
    void rejectsUnknownCode() {
      assertThatThrownBy(() -> registry.find("test.unknown"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown condition type code: test.unknown");
    }
  }
}
