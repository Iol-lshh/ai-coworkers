package ai.coworkers.core.model.template.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.StepTemplateData.BranchData;
import ai.coworkers.core.model.template.step.StepTemplateData.LoopData;
import ai.coworkers.core.model.template.step.StepTemplateData.ParallelData;
import ai.coworkers.core.model.template.step.condition.StepConditionCodec;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.template.step.task.TaskStepTemplate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepTemplateCodec")
class StepTemplateCodecTest {

  private static final StepConditionCodec CONDITION_CODEC = new StepConditionCodec();
  private static final StepTemplateCodec CODEC = new StepTemplateCodec(CONDITION_CODEC);

  private static final LeafData APPROVED =
      new LeafData("core.bool", Map.of("key", "approved", "expected", "true"));
  private static final UUID ID = UUID.randomUUID();
  private static final UUID FIRST = UUID.randomUUID();
  private static final UUID SECOND = UUID.randomUUID();

  private static final BranchData BRANCH = new BranchData(ID, 2L, APPROVED, FIRST, SECOND);
  private static final LoopData LOOP = new LoopData(ID, 2L, APPROVED, FIRST, SECOND);
  private static final ParallelData PARALLEL = new ParallelData(ID, 2L, List.of(FIRST), SECOND);

  private record TestTaskTemplate(StepTemplateId id) implements TaskStepTemplate {
    @Override
    public Long version() {
      return 1L;
    }

    @Override
    public @Nullable StepTemplateId next() {
      return null;
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @Test
    @DisplayName("분기 저장 형태로 분기 템플릿을 만듦")
    void decodesBranch() {
      assertThat(CODEC.decode(BRANCH))
          .isEqualTo(
              new BranchStepTemplate(
                  new StepTemplateId(ID),
                  2L,
                  CONDITION_CODEC.decode(APPROVED),
                  new StepTemplateId(FIRST),
                  new StepTemplateId(SECOND)));
    }

    @Test
    @DisplayName("반복 저장 형태로 반복 템플릿을 만듦")
    void decodesLoop() {
      assertThat(CODEC.decode(LOOP))
          .isEqualTo(
              new LoopStepTemplate(
                  new StepTemplateId(ID),
                  2L,
                  CONDITION_CODEC.decode(APPROVED),
                  new StepTemplateId(FIRST),
                  new StepTemplateId(SECOND)));
    }

    @Test
    @DisplayName("병렬 저장 형태로 병렬 템플릿을 만듦")
    void decodesParallel() {
      assertThat(CODEC.decode(PARALLEL))
          .isEqualTo(
              new ParallelStepTemplate(
                  new StepTemplateId(ID),
                  2L,
                  List.of(new StepTemplateId(FIRST)),
                  new StepTemplateId(SECOND)));
    }

    @Test
    @DisplayName("등록되지 않은 조건 종류를 쓰면 거부함")
    void rejectsUnknownConditionType() {
      BranchData unknown =
          new BranchData(ID, 2L, new LeafData("test.unknown", Map.of()), FIRST, SECOND);

      assertThatThrownBy(() -> CODEC.decode(unknown)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("만든 템플릿을 다시 같은 저장 형태로 바꿈")
    void encodesBackToSameData() {
      assertThat(List.of(BRANCH, LOOP, PARALLEL))
          .allSatisfy(data -> assertThat(CODEC.encode(CODEC.decode(data))).isEqualTo(data));
    }

    @Test
    @DisplayName("저장 형태가 없는 작업 템플릿이면 거부함")
    void rejectsTaskTemplate() {
      assertThatThrownBy(() -> CODEC.encode(new TestTaskTemplate(StepTemplateId.generate())))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("unsupported template type");
    }
  }
}
