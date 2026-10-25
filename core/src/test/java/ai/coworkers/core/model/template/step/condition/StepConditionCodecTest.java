package ai.coworkers.core.model.template.step.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.template.step.condition.StepConditionData.AllOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.AnyOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.NotData;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StepConditionCodec")
class StepConditionCodecTest {

  private static final BoolConditionType BOOL = new BoolConditionType();
  private static final PresentConditionType PRESENT = new PresentConditionType();
  private static final StepConditionCodec CODEC = new StepConditionCodec();

  private static final Map<String, String> APPROVED = Map.of("key", "approved", "expected", "true");
  private static final Map<String, String> COMMENT = Map.of("key", "comment");

  private static final StepCondition TREE =
      new AllOf(
          List.of(
              new Leaf(BOOL, APPROVED),
              new AnyOf(List.of(new Not(new Leaf(PRESENT, COMMENT)), new Leaf(BOOL, APPROVED)))));

  private static final StepConditionData DATA =
      new AllOfData(
          List.of(
              new LeafData("core.bool", APPROVED),
              new AnyOfData(
                  List.of(
                      new NotData(new LeafData("core.present", COMMENT)),
                      new LeafData("core.bool", APPROVED)))));

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"core.present", "core.bool", "core.number", "core.text"})
    @DisplayName("기본 조건 종류를 항상 등록함")
    void registersBuiltInTypes(String code) {
      LeafData data =
          new LeafData(
              code, Map.of("key", "k", "expected", "true", "operator", "EQ", "value", "1"));

      assertThat(CODEC.encode(CODEC.decode(data))).isEqualTo(data);
    }

    @Test
    @DisplayName("확장 조건 종류를 함께 등록함")
    void registersExtensions() {
      StepConditionCodec codec =
          new StepConditionCodec(List.of(new FixedStepConditionType("test.fixed", true)));

      assertThat(codec.decode(new LeafData("test.fixed", Map.of())))
          .isEqualTo(FixedStepConditionType.leaf(true));
    }

    @Test
    @DisplayName("확장 조건 종류가 기본 조건 종류의 코드를 쓰면 거부함")
    void rejectsBuiltInCode() {
      assertThatThrownBy(
              () -> new StepConditionCodec(List.of(new FixedStepConditionType("core.bool", true))))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("duplicate condition type code: core.bool");
    }
  }

  @Nested
  @DisplayName("encode")
  class Encode {

    @Test
    @DisplayName("조건 트리를 같은 구조의 저장 형태로 바꿈")
    void encodesTree() {
      assertThat(CODEC.encode(TREE)).isEqualTo(DATA);
    }

    @Test
    @DisplayName("등록되지 않은 조건 종류를 쓰는 말단 노드가 있으면 거부함")
    void rejectsUnregisteredType() {
      assertThatThrownBy(() -> CODEC.encode(FixedStepConditionType.leaf(true)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown condition type code: test.fixed");
    }
  }

  @Nested
  @DisplayName("decode")
  class Decode {

    @Test
    @DisplayName("저장 형태로 같은 구조의 조건 트리를 만듦")
    void decodesTree() {
      assertThat(CODEC.decode(DATA)).isEqualTo(TREE);
    }

    @Test
    @DisplayName("등록되지 않은 코드가 있으면 거부함")
    void rejectsUnknownCode() {
      assertThatThrownBy(() -> CODEC.decode(new LeafData("test.unknown", Map.of())))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("unknown condition type code: test.unknown");
    }

    @Test
    @DisplayName("자식이 없는 조합 노드가 있으면 거부함")
    void rejectsEmptyChildren() {
      assertThatThrownBy(() -> CODEC.decode(new NotData(new AnyOfData(List.of()))))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("conditions must not be empty");
    }

    @Test
    @DisplayName("조건 종류가 거부하는 설정값이 있으면 거부함")
    void rejectsInvalidConfig() {
      assertThatThrownBy(() -> CODEC.decode(new LeafData("core.bool", Map.of("key", "approved"))))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("missing condition config: expected");
    }
  }
}
