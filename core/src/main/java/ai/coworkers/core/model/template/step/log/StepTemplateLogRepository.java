package ai.coworkers.core.model.template.step.log;

import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateId;
import java.util.Optional;

/** 템플릿 로그 저장소. 추가만 하고 수정하지 않음. */
public interface StepTemplateLogRepository {

  /**
   * 로그를 추가함.
   *
   * @param log 추가할 로그
   */
  void append(StepTemplateLog log);

  /**
   * 식별자와 버전으로 그 시점의 스냅샷을 찾음.
   *
   * @param id 템플릿 식별자
   * @param version 템플릿 버전
   * @return 스냅샷. 없으면 빈 값
   */
  Optional<StepTemplate> findSnapshot(StepTemplateId id, Long version);
}
