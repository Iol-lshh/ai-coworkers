package ai.coworkers.core.model.workflow.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.input.StepInput;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Leaf")
class LeafTest {

  private static final StepInput INPUT = StepInput.empty();

  private record ConfiguredConditionType() implements StepConditionType {
    @Override
    public String code() {
      return "test.configured";
    }

    @Override
    public void validate(Map<String, String> config) {
      if (!config.containsKey("satisfied")) {
        throw new IllegalArgumentException("missing condition config: satisfied");
      }
    }

    @Override
    public boolean isSatisfiedBy(Map<String, String> config, StepInput input) {
      return Boolean.parseBoolean(config.get("satisfied"));
    }
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("설정값을 불변으로 복사함")
    void copiesConfig() {
      Map<String, String> config = new HashMap<>(Map.of("satisfied", "true"));
      Leaf leaf = new Leaf(new ConfiguredConditionType(), config);
      config.put("other", "value");

      assertThat(leaf.config()).containsExactly(Map.entry("satisfied", "true")).isUnmodifiable();
    }

    @Test
    @DisplayName("조건 종류가 설정값을 거부하면 거부함")
    void rejectsInvalidConfig() {
      assertThatThrownBy(() -> new Leaf(new ConfiguredConditionType(), Map.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("missing condition config: satisfied");
    }
  }

  @Nested
  @DisplayName("isSatisfiedBy")
  class IsSatisfiedBy {

    @ParameterizedTest(name = "satisfied={0}")
    @ValueSource(booleans = {true, false})
    @DisplayName("설정값과 입력으로 조건 종류의 판정을 반환함")
    void delegatesToConditionType(boolean satisfied) {
      Leaf leaf =
          new Leaf(new ConfiguredConditionType(), Map.of("satisfied", String.valueOf(satisfied)));

      assertThat(leaf.isSatisfiedBy(INPUT)).isEqualTo(satisfied);
    }
  }
}
