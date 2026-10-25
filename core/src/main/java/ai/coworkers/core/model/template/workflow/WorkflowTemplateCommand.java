package ai.coworkers.core.model.template.workflow;

import ai.coworkers.core.common.PageCommand;
import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.List;

/** 워크플로우 틀 요청 */
public sealed interface WorkflowTemplateCommand {

  /**
   * 워크플로우 틀 등록 요청. 식별자와 버전이 정해지면 틀을 만듦.
   *
   * @param start 실행을 시작하는 템플릿
   * @param steps 틀에 속한 템플릿
   */
  record Create(StepTemplateId start, List<StepTemplateId> steps)
      implements WorkflowTemplateCommand {

    public Create {
      steps = List.copyOf(steps);
    }

    /**
     * 식별자와 버전을 붙여 틀을 만듦.
     *
     * @param id 워크플로우 틀 식별자
     * @param version 워크플로우 틀 버전
     * @return 만든 틀
     */
    public WorkflowTemplate toTemplate(WorkflowTemplateId id, Long version) {
      return new WorkflowTemplate(id, version, start, steps);
    }
  }

  /**
   * 워크플로우 틀 수정 요청
   *
   * @param id 수정할 틀 식별자
   * @param expectedVersion 수정 기준으로 삼은 현재본 버전
   * @param definition 수정한 정의
   */
  record Update(WorkflowTemplateId id, Long expectedVersion, Create definition)
      implements WorkflowTemplateCommand {}

  /**
   * 워크플로우 틀 페이지 조회 요청
   *
   * @param page 0부터 시작하는 페이지 번호
   * @param size 페이지 크기
   */
  record FindPage(long page, long size) implements WorkflowTemplateCommand, PageCommand {}
}
