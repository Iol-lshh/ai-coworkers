package ai.coworkers.core.model.template.workflow;

import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 시작 템플릿과 템플릿 목록을 묶어 슬롯으로 찍어낼 워크플로우의 틀
 *
 * @param id 워크플로우 틀 식별자
 * @param version 워크플로우 틀 버전
 * @param start 실행을 시작하는 템플릿
 * @param steps 틀에 속한 템플릿
 */
public record WorkflowTemplate(
    WorkflowTemplateId id, Long version, StepTemplateId start, List<StepTemplateId> steps) {

  /** 템플릿이 중복되거나 시작 템플릿이 틀에 없으면 거부함. */
  public WorkflowTemplate {
    steps = List.copyOf(steps);
    Set<StepTemplateId> distinct = new HashSet<>();
    for (StepTemplateId step : steps) {
      if (!distinct.add(step)) {
        throw new IllegalArgumentException("duplicate step template id: " + step);
      }
    }
    if (!distinct.contains(start)) {
      throw new IllegalArgumentException("start not in steps: " + start);
    }
  }
}
