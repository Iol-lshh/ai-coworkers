package ai.coworkers.core.model.template.step.condition;

import ai.coworkers.core.model.template.step.condition.StepConditionData.AllOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.AnyOfData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.core.model.template.step.condition.StepConditionData.NotData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** 조건 트리를 저장 형태로 바꾸고, 저장 형태로 조건 트리를 만드는 진입점 */
public final class StepConditionCodec {

  private static final List<StepConditionType> BUILT_IN =
      List.of(
          new PresentConditionType(),
          new BoolConditionType(),
          new NumberConditionType(),
          new TextConditionType());

  private final StepConditionTypeRegistry registry;

  /** 기본 조건 종류만 등록함. */
  public StepConditionCodec() {
    this(List.of());
  }

  /**
   * 기본 조건 종류와 확장 조건 종류를 등록함. 코드가 중복되면 거부함.
   *
   * @param extensions 등록할 확장 조건 종류
   */
  public StepConditionCodec(Collection<? extends StepConditionType> extensions) {
    List<StepConditionType> conditionTypes = new ArrayList<>(BUILT_IN);
    conditionTypes.addAll(extensions);
    this.registry = new StepConditionTypeRegistry(conditionTypes);
  }

  /**
   * 조건 트리를 저장 형태로 바꿈. 등록되지 않은 조건 종류를 쓰는 말단 노드가 있으면 거부함.
   *
   * @param condition 조건 트리
   * @return 저장 형태
   */
  public StepConditionData encode(StepCondition condition) {
    return switch (condition) {
      case AllOf allOf -> new AllOfData(encodeAll(allOf.conditions()));
      case AnyOf anyOf -> new AnyOfData(encodeAll(anyOf.conditions()));
      case Not not -> new NotData(encode(not.condition()));
      case Leaf leaf -> encodeLeaf(leaf);
    };
  }

  /**
   * 저장 형태로 조건 트리를 만듦. 등록되지 않은 코드, 자식이 없는 조합 노드, 조건 종류가 거부하는 설정값이 있으면 거부함.
   *
   * @param data 저장 형태
   * @return 조건 트리
   */
  public StepCondition decode(StepConditionData data) {
    return switch (data) {
      case AllOfData allOf -> new AllOf(decodeAll(allOf.children()));
      case AnyOfData anyOf -> new AnyOf(decodeAll(anyOf.children()));
      case NotData not -> new Not(decode(not.child()));
      case LeafData leaf -> new Leaf(registry.find(leaf.code()), leaf.config());
    };
  }

  private List<StepConditionData> encodeAll(List<StepCondition> conditions) {
    return conditions.stream().map(this::encode).toList();
  }

  private List<StepCondition> decodeAll(List<StepConditionData> children) {
    return children.stream().map(this::decode).toList();
  }

  private LeafData encodeLeaf(Leaf leaf) {
    StepConditionType type = registry.find(leaf.type().code());
    return new LeafData(type.code(), leaf.config());
  }
}
