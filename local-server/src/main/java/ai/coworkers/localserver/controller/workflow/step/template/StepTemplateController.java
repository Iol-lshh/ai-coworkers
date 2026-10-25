package ai.coworkers.localserver.controller.workflow.step.template;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.workflow.step.condition.StepCondition;
import ai.coworkers.core.model.workflow.step.condition.StepConditionCodec;
import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.FindPage;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.localserver.service.workflow.step.template.StepTemplateService;
import ai.coworkers.serd.json.step.StepConditionJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** 템플릿 생성·조회·수정·삭제 API */
@Tag(name = "StepTemplate", description = "스텝 템플릿")
@RestController
@RequestMapping("/api/step-templates")
@RequiredArgsConstructor
class StepTemplateController {

  private final StepTemplateService service;
  private final StepConditionCodec conditionCodec;
  private final StepConditionJson conditionJson;

  /**
   * 템플릿을 등록함.
   *
   * @param request 템플릿 정의
   * @return 등록한 현재본
   */
  @Operation(summary = "템플릿 등록", description = "새 식별자를 발급하고 첫 버전으로 등록함.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  StepTemplateResponseDto create(@RequestBody StepTemplateRequestDto request) {
    return toResponse(service.create(request.toCommand(this::toCondition)));
  }

  /**
   * 현재본을 조회함. 없으면 404로 응답함.
   *
   * @param id 템플릿 식별 값
   * @return 현재본
   */
  @Operation(summary = "템플릿 조회")
  @GetMapping("/{id}")
  StepTemplateResponseDto findById(@PathVariable UUID id) {
    return service
        .findById(new StepTemplateId(id))
        .map(this::toResponse)
        .orElseThrow(() -> new NoSuchElementException("unknown template id: " + id));
  }

  /**
   * 현재본을 페이지 단위로 조회함.
   *
   * @param page 0부터 시작하는 페이지 번호
   * @param size 페이지 크기
   * @return 전체 개수와 해당 페이지의 현재본
   */
  @Operation(summary = "템플릿 페이지 조회", description = "식별자 순으로 정렬함.")
  @GetMapping
  StepTemplatePageResponseDto page(
      @Parameter(description = "0부터 시작하는 페이지 번호") @RequestParam(defaultValue = "0") long page,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") long size) {
    Page<StepTemplate> result = service.page(new FindPage(page, size));
    return new StepTemplatePageResponseDto(
        result.total(), result.list().stream().map(this::toResponse).toList());
  }

  /**
   * 현재본을 수정함. 기준 버전이 현재본과 다르면 409로 응답함.
   *
   * @param id 템플릿 식별 값
   * @param request 수정 요청
   * @return 수정한 현재본
   */
  @Operation(summary = "템플릿 수정", description = "기준 버전이 현재본과 같을 때만 수정하고 버전을 올림.")
  @PutMapping("/{id}")
  StepTemplateResponseDto update(
      @PathVariable UUID id, @RequestBody StepTemplateUpdateRequestDto request) {
    return toResponse(service.update(request.toCommand(id, this::toCondition)));
  }

  /**
   * 현재본을 삭제함. 로그는 남음.
   *
   * @param id 템플릿 식별 값
   */
  @Operation(summary = "템플릿 삭제", description = "현재본만 삭제하고 로그는 남김.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable UUID id) {
    service.delete(new StepTemplateId(id));
  }

  private StepCondition toCondition(JsonNode node) {
    return conditionCodec.decode(conditionJson.fromNode(node));
  }

  private StepTemplateResponseDto toResponse(StepTemplate template) {
    return StepTemplateResponseDto.from(
        template, condition -> conditionJson.toNode(conditionCodec.encode(condition)));
  }
}
