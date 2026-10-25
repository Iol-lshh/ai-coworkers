package ai.coworkers.localserver.controller.step.template;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 템플릿 현재본 페이지 응답 */
@Getter
@RequiredArgsConstructor
@Schema(name = "StepTemplatePageResponse")
public class StepTemplatePageResponseDto {

  @Schema(description = "전체 현재본 개수")
  private final long total;

  @Schema(description = "해당 페이지의 현재본")
  private final List<StepTemplateResponseDto> list;
}
