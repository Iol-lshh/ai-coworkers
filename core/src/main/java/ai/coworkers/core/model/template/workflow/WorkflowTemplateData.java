package ai.coworkers.core.model.template.workflow;

import java.util.List;
import java.util.UUID;

/**
 * 워크플로우 틀을 JDK 타입만으로 나타낸 저장 형태
 *
 * @param id 식별 값
 * @param version 버전
 * @param start 실행을 시작하는 템플릿 식별 값
 * @param steps 틀에 속한 템플릿 식별 값
 */
public record WorkflowTemplateData(UUID id, Long version, UUID start, List<UUID> steps) {

  public WorkflowTemplateData {
    steps = List.copyOf(steps);
  }
}
