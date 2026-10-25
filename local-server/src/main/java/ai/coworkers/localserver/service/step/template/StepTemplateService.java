package ai.coworkers.localserver.service.step.template;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateCommand;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.StepTemplateProcessor;
import ai.coworkers.core.model.template.step.StepTemplateRepository;
import ai.coworkers.core.model.template.step.log.StepTemplateLogRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 템플릿 생성·조회·수정·삭제 유스케이스의 트랜잭션 경계. 흐름과 규칙은 도메인 서비스에 위임함. */
@Service
public class StepTemplateService {

  private final StepTemplateProcessor processor;

  /**
   * 저장소 동작으로 템플릿 도메인 서비스를 만듦.
   *
   * @param templateRepository 템플릿 현재본 저장소
   * @param logRepository 템플릿 로그 저장소
   */
  public StepTemplateService(
      StepTemplateRepository templateRepository, StepTemplateLogRepository logRepository) {
    this.processor =
        new StepTemplateProcessor(
            templateRepository::existsById,
            templateRepository::findById,
            templateRepository::find,
            templateRepository::save,
            templateRepository::deleteById,
            logRepository::append);
  }

  /**
   * 새 식별자를 발급하고 첫 버전으로 템플릿을 등록함.
   *
   * @param command 등록 요청
   * @return 등록한 현재본
   */
  @Transactional
  public StepTemplate create(StepTemplateCommand.Create command) {
    return processor.create(command);
  }

  /**
   * 식별자로 현재본을 찾음.
   *
   * @param id 템플릿 식별자
   * @return 현재본. 없으면 빈 값
   */
  @Transactional(readOnly = true)
  public Optional<StepTemplate> findById(StepTemplateId id) {
    return processor.findById(id);
  }

  /**
   * 현재본을 페이지 단위로 조회함.
   *
   * @param command 페이지 조회 요청
   * @return 전체 개수와 해당 페이지의 현재본
   */
  @Transactional(readOnly = true)
  public Page<StepTemplate> page(StepTemplateCommand.FindPage command) {
    return processor.page(command);
  }

  /**
   * 현재본을 수정한 정의로 바꾸고 버전을 올림.
   *
   * @param command 수정 요청
   * @return 수정한 현재본
   */
  @Transactional
  public StepTemplate update(StepTemplateCommand.Update command) {
    return processor.update(command);
  }

  /**
   * 현재본을 삭제함.
   *
   * @param id 템플릿 식별자
   */
  @Transactional
  public void delete(StepTemplateId id) {
    processor.delete(id);
  }
}
