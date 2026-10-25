package ai.coworkers.localserver.controller.step.template;

import ai.coworkers.core.model.template.step.BranchStepTemplate;
import ai.coworkers.core.model.template.step.LoopStepTemplate;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.StepCondition;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import tools.jackson.databind.JsonNode;

/** 템플릿 현재본 응답. {@code kind}로 템플릿 종류를 구분함. */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StepTemplateResponseDto.Branch.class, name = "BRANCH"),
  @JsonSubTypes.Type(value = StepTemplateResponseDto.Loop.class, name = "LOOP"),
  @JsonSubTypes.Type(value = StepTemplateResponseDto.Parallel.class, name = "PARALLEL")
})
@Schema(
    name = "StepTemplateResponse",
    description = "템플릿 현재본",
    oneOf = {
      StepTemplateResponseDto.Branch.class,
      StepTemplateResponseDto.Loop.class,
      StepTemplateResponseDto.Parallel.class
    },
    discriminatorProperty = "kind",
    discriminatorMapping = {
      @DiscriminatorMapping(value = "BRANCH", schema = StepTemplateResponseDto.Branch.class),
      @DiscriminatorMapping(value = "LOOP", schema = StepTemplateResponseDto.Loop.class),
      @DiscriminatorMapping(value = "PARALLEL", schema = StepTemplateResponseDto.Parallel.class)
    })
public abstract class StepTemplateResponseDto {

  @Schema(description = "템플릿 식별자")
  private final UUID id;

  @Schema(description = "템플릿 버전")
  private final Long version;

  StepTemplateResponseDto(StepTemplate template) {
    this.id = template.id().value();
    this.version = template.version();
  }

  /**
   * 템플릿으로 응답을 만듦. 응답 형태가 없는 템플릿 종류면 거부함.
   *
   * @param template 템플릿 현재본
   * @param nodeOf 조건 트리를 JSON으로 바꾸는 함수
   * @return 응답
   */
  static StepTemplateResponseDto from(
      StepTemplate template, Function<StepCondition, JsonNode> nodeOf) {
    return switch (template) {
      case BranchStepTemplate branch -> new Branch(branch, nodeOf.apply(branch.condition()));
      case LoopStepTemplate loop -> new Loop(loop, nodeOf.apply(loop.condition()));
      case ParallelStepTemplate parallel -> new Parallel(parallel);
      default ->
          throw new IllegalArgumentException(
              "unsupported template type: " + template.getClass().getName());
    };
  }

  /** 분기 템플릿 응답 */
  @Getter
  @Schema(name = "BranchStepTemplateResponse")
  public static class Branch extends StepTemplateResponseDto {

    @Schema(description = "판정 조건 트리")
    private final JsonNode condition;

    @Schema(description = "조건을 만족할 때 이어지는 템플릿 식별자")
    private final UUID whenSatisfied;

    @Schema(description = "조건을 만족하지 않을 때 이어지는 템플릿 식별자")
    private final UUID otherwise;

    Branch(BranchStepTemplate template, JsonNode condition) {
      super(template);
      this.condition = condition;
      this.whenSatisfied = template.whenSatisfied().value();
      this.otherwise = template.otherwise().value();
    }
  }

  /** 반복 템플릿 응답 */
  @Getter
  @Schema(name = "LoopStepTemplateResponse")
  public static class Loop extends StepTemplateResponseDto {

    @Schema(description = "반복 조건 트리")
    private final JsonNode condition;

    @Schema(description = "반복할 때 이어지는 템플릿 식별자")
    private final UUID body;

    @Schema(description = "반복을 끝낼 때 이어지는 템플릿 식별자")
    private final UUID exit;

    Loop(LoopStepTemplate template, JsonNode condition) {
      super(template);
      this.condition = condition;
      this.body = template.body().value();
      this.exit = template.exit().value();
    }
  }

  /** 병렬 템플릿 응답 */
  @Getter
  @Schema(name = "ParallelStepTemplateResponse")
  public static class Parallel extends StepTemplateResponseDto {

    @Schema(description = "동시에 시작하는 템플릿 식별자")
    private final List<UUID> branches;

    @Schema(description = "모든 갈래가 끝난 뒤 이어지는 템플릿 식별자")
    private final UUID join;

    Parallel(ParallelStepTemplate template) {
      super(template);
      this.branches = template.branches().stream().map(StepTemplateId::value).toList();
      this.join = template.join().value();
    }
  }
}
