package ai.coworkers.core.model.template.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WorkflowTemplate")
class WorkflowTemplateTest {

  private static final WorkflowTemplateId ID = WorkflowTemplateId.generate();
  private static final StepTemplateId START = StepTemplateId.generate();
  private static final StepTemplateId NEXT = StepTemplateId.generate();

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("만든 뒤 넘긴 목록을 바꿔도 틀은 바뀌지 않음")
    void copiesSteps() {
      List<StepTemplateId> steps = new ArrayList<>(List.of(START));
      WorkflowTemplate template = new WorkflowTemplate(ID, 1L, START, steps);

      steps.add(NEXT);

      assertThat(template.steps()).containsExactly(START);
    }

    @Test
    @DisplayName("템플릿이 중복되면 거부함")
    void rejectsDuplicateStep() {
      assertThatThrownBy(() -> new WorkflowTemplate(ID, 1L, START, List.of(START, NEXT, START)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate step template id: " + START);
    }

    @Test
    @DisplayName("시작 템플릿이 틀에 없으면 거부함")
    void rejectsStartNotInSteps() {
      assertThatThrownBy(() -> new WorkflowTemplate(ID, 1L, START, List.of(NEXT)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("start not in steps: " + START);
    }
  }
}
