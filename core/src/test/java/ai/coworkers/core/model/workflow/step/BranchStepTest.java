package ai.coworkers.core.model.workflow.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BranchStep")
class BranchStepTest {

    private static final TaskType ANALYZE = () -> "test.analyze";
    private static final TaskStep THEN = new TaskStep("then", ANALYZE, Map.of());
    private static final TaskStep ELSE = new TaskStep("else", ANALYZE, Map.of());

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("참 단계와 거짓 단계를 순서대로 보관함")
        void keepsSteps() {
            BranchStep branch = new BranchStep("branch", "has-issues", List.of(THEN), List.of(ELSE));

            assertThat(branch.thenSteps()).containsExactly(THEN);
            assertThat(branch.elseSteps()).containsExactly(ELSE);
        }

        @Test
        @DisplayName("거짓 단계가 비어 있으면 허용함")
        void allowsEmptyElseSteps() {
            BranchStep branch = new BranchStep("branch", "has-issues", List.of(THEN), List.of());

            assertThat(branch.elseSteps()).isEmpty();
        }

        @Test
        @DisplayName("전달한 목록이 바뀌어도 보관한 단계는 바뀌지 않음")
        void copiesSteps() {
            List<StepDefinition> thenSteps = new ArrayList<>(List.of(THEN));
            List<StepDefinition> elseSteps = new ArrayList<>(List.of(ELSE));
            BranchStep branch = new BranchStep("branch", "has-issues", thenSteps, elseSteps);

            thenSteps.add(ELSE);
            elseSteps.clear();

            assertThat(branch.thenSteps()).containsExactly(THEN);
            assertThat(branch.elseSteps()).containsExactly(ELSE);
        }

        @ParameterizedTest(name = "key [{0}]")
        @EmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("키가 비어 있으면 거부함")
        void rejectsBlankKey(String key) {
            assertThatThrownBy(() -> new BranchStep(key, "has-issues", List.of(THEN), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("key must not be blank");
        }

        @ParameterizedTest(name = "conditionCode [{0}]")
        @EmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("조건 코드가 비어 있으면 거부함")
        void rejectsBlankConditionCode(String conditionCode) {
            assertThatThrownBy(() -> new BranchStep("branch", conditionCode, List.of(THEN), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("conditionCode must not be blank");
        }

        @ParameterizedTest(name = "thenSteps [{0}]")
        @EmptySource
        @DisplayName("참 단계가 비어 있으면 거부함")
        void rejectsEmptyThenSteps(List<StepDefinition> thenSteps) {
            assertThatThrownBy(() -> new BranchStep("branch", "has-issues", thenSteps, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("thenSteps must not be empty");
        }
    }
}
