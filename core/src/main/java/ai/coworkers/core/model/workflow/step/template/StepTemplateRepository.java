package ai.coworkers.core.model.workflow.step.template;

import ai.coworkers.core.common.Page;
import java.util.Optional;

/** 템플릿 현재본 저장소 */
public interface StepTemplateRepository {

  /**
   * 식별자로 현재본을 찾음.
   *
   * @param id 템플릿 식별자
   * @return 현재본. 없으면 빈 값
   */
  Optional<StepTemplate> findById(StepTemplateId id);

  /**
   * 현재본을 페이지 단위로 반환함.
   *
   * @param command 페이지 조회 요청
   * @return 전체 개수와 해당 페이지의 현재본
   */
  Page<StepTemplate> find(StepTemplateCommand.FindPage command);

  /**
   * 식별자의 현재본이 있는지 확인함.
   *
   * @param id 템플릿 식별자
   * @return 있으면 참
   */
  boolean existsById(StepTemplateId id);

  /**
   * 현재본을 저장함. 같은 식별자의 현재본이 있으면 갱신함.
   *
   * @param template 저장할 현재본
   */
  void save(StepTemplate template);

  /**
   * 식별자의 현재본을 삭제함.
   *
   * @param id 템플릿 식별자
   */
  void deleteById(StepTemplateId id);
}
