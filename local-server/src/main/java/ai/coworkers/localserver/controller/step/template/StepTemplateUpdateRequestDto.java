package ai.coworkers.localserver.controller.step.template;

import static ai.coworkers.localserver.controller.step.template.StepTemplateRequestDto.required;

import ai.coworkers.core.model.template.step.StepTemplateCommand;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.StepCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

/** 템플릿 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@Schema(name = "StepTemplateUpdateRequest")
public class StepTemplateUpdateRequestDto {

  @Schema(description = "수정 기준으로 삼은 현재본 버전", requiredMode = Schema.RequiredMode.REQUIRED)
  private @Nullable Long expectedVersion;

  @Schema(description = "수정한 정의", requiredMode = Schema.RequiredMode.REQUIRED)
  private @Nullable StepTemplateRequestDto definition;

  /**
   * 수정 요청으로 바꿈. 필수 필드가 없으면 거부함.
   *
   * @param id 수정할 템플릿 식별 값
   * @param conditionOf 조건 트리 JSON을 조건 트리로 바꾸는 함수
   * @return 수정 요청
   */
  StepTemplateCommand.Update toCommand(UUID id, Function<JsonNode, StepCondition> conditionOf) {
    return new StepTemplateCommand.Update(
        new StepTemplateId(id),
        required(expectedVersion, "expectedVersion"),
        required(definition, "definition").toCommand(conditionOf));
  }
}
