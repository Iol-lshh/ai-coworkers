package ai.coworkers.localserver.controller.workflow.step.template;

import ai.coworkers.core.model.workflow.step.condition.StepCondition;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.CreateBranch;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.CreateLoop;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.CreateParallel;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

/** 템플릿 정의 요청. {@code kind}로 템플릿 종류를 구분함. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StepTemplateRequestDto.Branch.class, name = "BRANCH"),
  @JsonSubTypes.Type(value = StepTemplateRequestDto.Loop.class, name = "LOOP"),
  @JsonSubTypes.Type(value = StepTemplateRequestDto.Parallel.class, name = "PARALLEL")
})
@Schema(
    name = "StepTemplateRequest",
    description = "템플릿 정의",
    oneOf = {
      StepTemplateRequestDto.Branch.class,
      StepTemplateRequestDto.Loop.class,
      StepTemplateRequestDto.Parallel.class
    },
    discriminatorProperty = "kind",
    discriminatorMapping = {
      @DiscriminatorMapping(value = "BRANCH", schema = StepTemplateRequestDto.Branch.class),
      @DiscriminatorMapping(value = "LOOP", schema = StepTemplateRequestDto.Loop.class),
      @DiscriminatorMapping(value = "PARALLEL", schema = StepTemplateRequestDto.Parallel.class)
    })
public abstract class StepTemplateRequestDto {

  /**
   * 등록 요청으로 바꿈. 필수 필드가 없으면 거부함.
   *
   * @param conditionOf 조건 트리 JSON을 조건 트리로 바꾸는 함수
   * @return 등록 요청
   */
  abstract StepTemplateCommand.Create toCommand(Function<JsonNode, StepCondition> conditionOf);

  /** 분기 템플릿 정의 */
  @Getter
  @Setter
  @NoArgsConstructor
  @Schema(name = "BranchStepTemplateRequest")
  public static class Branch extends StepTemplateRequestDto {

    @Schema(description = "판정 조건 트리", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable JsonNode condition;

    @Schema(description = "조건을 만족할 때 이어지는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable UUID whenSatisfied;

    @Schema(description = "조건을 만족하지 않을 때 이어지는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable UUID otherwise;

    @Override
    StepTemplateCommand.Create toCommand(Function<JsonNode, StepCondition> conditionOf) {
      return new CreateBranch(
          conditionOf.apply(required(condition, "condition")),
          new StepTemplateId(required(whenSatisfied, "whenSatisfied")),
          new StepTemplateId(required(otherwise, "otherwise")));
    }
  }

  /** 반복 템플릿 정의 */
  @Getter
  @Setter
  @NoArgsConstructor
  @Schema(name = "LoopStepTemplateRequest")
  public static class Loop extends StepTemplateRequestDto {

    @Schema(description = "반복 조건 트리", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable JsonNode condition;

    @Schema(description = "반복할 때 이어지는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable UUID body;

    @Schema(description = "반복을 끝낼 때 이어지는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable UUID exit;

    @Override
    StepTemplateCommand.Create toCommand(Function<JsonNode, StepCondition> conditionOf) {
      return new CreateLoop(
          conditionOf.apply(required(condition, "condition")),
          new StepTemplateId(required(body, "body")),
          new StepTemplateId(required(exit, "exit")));
    }
  }

  /** 병렬 템플릿 정의 */
  @Getter
  @Setter
  @NoArgsConstructor
  @Schema(name = "ParallelStepTemplateRequest")
  public static class Parallel extends StepTemplateRequestDto {

    @Schema(description = "동시에 시작하는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable List<UUID> branches;

    @Schema(description = "모든 갈래가 끝난 뒤 이어지는 템플릿 식별자", requiredMode = Schema.RequiredMode.REQUIRED)
    private @Nullable UUID join;

    @Override
    StepTemplateCommand.Create toCommand(Function<JsonNode, StepCondition> conditionOf) {
      return new CreateParallel(
          required(branches, "branches").stream().map(StepTemplateId::new).toList(),
          new StepTemplateId(required(join, "join")));
    }
  }

  /**
   * 필수 필드 값을 반환함. 없으면 거부함.
   *
   * @param value 필드 값
   * @param field 필드 이름
   * @param <T> 필드 타입
   * @return 필드 값
   */
  static <T> T required(@Nullable T value, String field) {
    if (value == null) {
      throw new IllegalArgumentException("missing field: " + field);
    }
    return value;
  }
}
