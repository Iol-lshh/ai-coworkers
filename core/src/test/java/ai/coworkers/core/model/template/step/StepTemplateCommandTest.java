package ai.coworkers.core.model.template.step;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.model.template.step.StepTemplateCommand.CreateBranch;
import ai.coworkers.core.model.template.step.StepTemplateCommand.CreateLoop;
import ai.coworkers.core.model.template.step.StepTemplateCommand.CreateParallel;
import ai.coworkers.core.model.template.step.condition.FixedStepConditionType;
import ai.coworkers.core.model.template.step.condition.StepCondition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepTemplateCommand")
class StepTemplateCommandTest {

  private static final StepTemplateId ID = StepTemplateId.generate();
  private static final StepTemplateId FIRST = StepTemplateId.generate();
  private static final StepTemplateId SECOND = StepTemplateId.generate();
  private static final StepCondition CONDITION = FixedStepConditionType.leaf(true);

  @Nested
  @DisplayName("CreateBranch.toTemplate")
  class CreateBranchToTemplate {

    @Test
    @DisplayName("식별자와 버전을 붙인 분기 템플릿을 만듦")
    void createsBranchTemplate() {
      assertThat(new CreateBranch(CONDITION, FIRST, SECOND).toTemplate(ID, 3L))
          .isEqualTo(new BranchStepTemplate(ID, 3L, CONDITION, FIRST, SECOND));
    }
  }

  @Nested
  @DisplayName("CreateLoop.toTemplate")
  class CreateLoopToTemplate {

    @Test
    @DisplayName("식별자와 버전을 붙인 반복 템플릿을 만듦")
    void createsLoopTemplate() {
      assertThat(new CreateLoop(CONDITION, FIRST, SECOND).toTemplate(ID, 3L))
          .isEqualTo(new LoopStepTemplate(ID, 3L, CONDITION, FIRST, SECOND));
    }
  }

  @Nested
  @DisplayName("CreateParallel.toTemplate")
  class CreateParallelToTemplate {

    @Test
    @DisplayName("식별자와 버전을 붙인 병렬 템플릿을 만듦")
    void createsParallelTemplate() {
      assertThat(new CreateParallel(List.of(FIRST), SECOND).toTemplate(ID, 3L))
          .isEqualTo(new ParallelStepTemplate(ID, 3L, List.of(FIRST), SECOND));
    }

    @Test
    @DisplayName("요청 생성 뒤 원본 목록을 바꿔도 갈래가 바뀌지 않음")
    void copiesBranches() {
      List<StepTemplateId> branches = new ArrayList<>(List.of(FIRST));
      CreateParallel command = new CreateParallel(branches, SECOND);

      branches.add(SECOND);

      assertThat(command.branches()).containsExactly(FIRST);
    }
  }
}
