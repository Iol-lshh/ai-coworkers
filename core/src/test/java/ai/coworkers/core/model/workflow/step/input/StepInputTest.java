package ai.coworkers.core.model.workflow.step.input;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.workflow.step.input.StepInputValue.BoolValue;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepInput")
class StepInputTest {

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("값 묶음을 불변으로 복사함")
    void copiesValues() {
      Map<String, StepInputValue> values = new HashMap<>(Map.of("approved", new BoolValue(true)));
      StepInput input = new StepInput(values);
      values.put("other", new BoolValue(false));

      assertThat(input.values())
          .containsExactly(Map.entry("approved", new BoolValue(true)))
          .isUnmodifiable();
    }
  }

  @Nested
  @DisplayName("empty")
  class Empty {

    @Test
    @DisplayName("값이 없는 입력을 반환함")
    void returnsInputWithoutValues() {
      assertThat(StepInput.empty().values()).isEmpty();
    }
  }

  @Nested
  @DisplayName("find")
  class Find {

    private final StepInput input = new StepInput(Map.of("approved", new BoolValue(true)));

    @Test
    @DisplayName("이름에 해당하는 값을 찾음")
    void findsValue() {
      assertThat(input.find("approved")).contains(new BoolValue(true));
    }

    @Test
    @DisplayName("이름에 해당하는 값이 없으면 빈 값을 반환함")
    void returnsEmptyForUnknownKey() {
      assertThat(input.find("unknown")).isEmpty();
    }
  }
}
