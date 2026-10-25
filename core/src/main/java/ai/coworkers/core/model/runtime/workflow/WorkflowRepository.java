package ai.coworkers.core.model.runtime.workflow;

import java.util.Optional;

/** 워크플로우 실행 저장소 */
public interface WorkflowRepository {

  /**
   * 식별자로 실행을 찾음.
   *
   * @param id 워크플로우 식별자
   * @return 실행. 없으면 빈 값
   */
  Optional<Workflow> findById(WorkflowId id);

  /**
   * 실행을 저장함. 같은 식별자의 실행이 있으면 갱신함.
   *
   * @param workflow 저장할 실행
   */
  void save(Workflow workflow);
}
