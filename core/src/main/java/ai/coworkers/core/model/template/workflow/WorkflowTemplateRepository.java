package ai.coworkers.core.model.template.workflow;

import ai.coworkers.core.common.Page;
import java.util.Optional;

/** 워크플로우 틀 현재본 저장소 */
public interface WorkflowTemplateRepository {

  /**
   * 식별자로 현재본을 찾음.
   *
   * @param id 워크플로우 틀 식별자
   * @return 현재본. 없으면 빈 값
   */
  Optional<WorkflowTemplate> findById(WorkflowTemplateId id);

  /**
   * 현재본을 페이지 단위로 반환함.
   *
   * @param command 페이지 조회 요청
   * @return 전체 개수와 해당 페이지의 현재본
   */
  Page<WorkflowTemplate> find(WorkflowTemplateCommand.FindPage command);

  /**
   * 식별자의 현재본이 있는지 확인함.
   *
   * @param id 워크플로우 틀 식별자
   * @return 있으면 참
   */
  boolean existsById(WorkflowTemplateId id);

  /**
   * 현재본을 저장함. 같은 식별자의 현재본이 있으면 갱신함.
   *
   * @param template 저장할 현재본
   */
  void save(WorkflowTemplate template);

  /**
   * 식별자의 현재본을 삭제함.
   *
   * @param id 워크플로우 틀 식별자
   */
  void deleteById(WorkflowTemplateId id);
}
