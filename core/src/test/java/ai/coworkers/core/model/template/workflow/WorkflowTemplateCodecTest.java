package ai.coworkers.core.model.template.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WorkflowTemplateCodec")
class WorkflowTemplateCodecTest {

  private static final WorkflowTemplateCodec CODEC = new WorkflowTemplateCodec();
  private static final StepTemplateId START = StepTemplateId.generate();
  private static final StepTemplateId NEXT = StepTemplateId.generate();
  private static final WorkflowTemplate TEMPLATE =
      new WorkflowTemplate(WorkflowTemplateId.generate(), 2L, START, List.of(START, NEXT));

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("식별 값, 버전, 시작 템플릿, 템플릿 목록을 순서대로 담음")
    void encodesFields() {
      assertThat(CODEC.encode(TEMPLATE))
          .isEqualTo(
              new WorkflowTemplateData(
                  TEMPLATE.id().value(), 2L, START.value(), List.of(START.value(), NEXT.value())));
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @Test
    @DisplayName("저장 형태로 같은 틀을 만듦")
    void decodesEncoded() {
      assertThat(CODEC.decode(CODEC.encode(TEMPLATE))).isEqualTo(TEMPLATE);
    }

    @Test
    @DisplayName("시작 템플릿이 목록에 없으면 거부함")
    void rejectsRuleViolation() {
      UUID start = UUID.randomUUID();
      WorkflowTemplateData data =
          new WorkflowTemplateData(UUID.randomUUID(), 1L, start, List.of(NEXT.value()));

      assertThatThrownBy(() -> CODEC.decode(data))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("start not in steps");
    }
  }
}
