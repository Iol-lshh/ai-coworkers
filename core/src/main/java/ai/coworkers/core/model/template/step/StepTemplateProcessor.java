package ai.coworkers.core.model.template.step;

import static ai.coworkers.core.common.PageSpecifications.VALID;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.common.UuidV7;
import ai.coworkers.core.model.template.step.log.StepTemplateLog;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** 템플릿 등록·조회·수정·삭제의 흐름과 규칙을 맡는 도메인 서비스. 현재본을 저장할 때마다 로그를 추가함. */
public final class StepTemplateProcessor {

  private static final Long FIRST_VERSION = 1L;

  private final Predicate<StepTemplateId> exists;
  private final Function<StepTemplateId, Optional<StepTemplate>> findById;
  private final Function<StepTemplateCommand.FindPage, Page<StepTemplate>> find;
  private final Consumer<StepTemplate> save;
  private final Consumer<StepTemplateId> delete;
  private final Consumer<StepTemplateLog> append;

  /**
   * 저장소 동작을 받음.
   *
   * @param exists 식별자의 현재본이 있는지 확인하는 동작
   * @param findById 식별자로 현재본을 찾는 동작
   * @param find 페이지 조회 요청으로 현재본 페이지를 반환하는 동작
   * @param save 현재본을 저장하는 동작. 같은 식별자의 현재본이 있으면 갱신함
   * @param delete 식별자의 현재본을 삭제하는 동작
   * @param append 로그를 추가하는 동작
   */
  public StepTemplateProcessor(
      Predicate<StepTemplateId> exists,
      Function<StepTemplateId, Optional<StepTemplate>> findById,
      Function<StepTemplateCommand.FindPage, Page<StepTemplate>> find,
      Consumer<StepTemplate> save,
      Consumer<StepTemplateId> delete,
      Consumer<StepTemplateLog> append) {
    this.exists = exists;
    this.findById = findById;
    this.find = find;
    this.save = save;
    this.delete = delete;
    this.append = append;
  }

  /**
   * 새 식별자를 발급하고 첫 버전으로 템플릿을 등록함.
   *
   * @param command 등록 요청
   * @return 등록한 현재본
   */
  public StepTemplate create(StepTemplateCommand.Create command) {
    StepTemplate registered = command.toTemplate(StepTemplateId.generate(), FIRST_VERSION);
    store(registered);
    return registered;
  }

  /**
   * 식별자로 현재본을 찾음.
   *
   * @param id 템플릿 식별자
   * @return 현재본. 없으면 빈 값
   */
  public Optional<StepTemplate> findById(StepTemplateId id) {
    return findById.apply(id);
  }

  /**
   * 현재본을 페이지 단위로 조회함. 페이지 번호가 음수이거나 크기가 1보다 작으면 거부함.
   *
   * @param command 페이지 조회 요청
   * @return 전체 개수와 해당 페이지의 현재본
   */
  public Page<StepTemplate> page(StepTemplateCommand.FindPage command) {
    VALID.isSatisfiedBy(command);
    return find.apply(command);
  }

  /**
   * 현재본을 수정한 정의로 바꾸고 버전을 올림. 현재본이 없거나, 기준 버전이 현재본과 다르면 충돌로 거부함.
   *
   * @param command 수정 요청
   * @return 수정한 현재본
   */
  public StepTemplate update(StepTemplateCommand.Update command) {
    StepTemplate current =
        findById(command.id())
            .orElseThrow(() -> new NoSuchElementException("unknown template id: " + command.id()));
    if (!current.version().equals(command.expectedVersion())) {
      throw new IllegalStateException(
          "stale template version: " + command.expectedVersion() + " != " + current.version());
    }
    StepTemplate updated = command.definition().toTemplate(command.id(), current.version() + 1);
    store(updated);
    return updated;
  }

  /**
   * 현재본을 삭제함. 로그는 남김. 현재본이 없으면 거부함.
   *
   * @param id 템플릿 식별자
   */
  public void delete(StepTemplateId id) {
    if (!exists.test(id)) {
      throw new NoSuchElementException("unknown template id: " + id);
    }
    delete.accept(id);
  }

  private void store(StepTemplate template) {
    save.accept(template);
    append.accept(new StepTemplateLog(UuidV7.generate(), template, LocalDateTime.now()));
  }
}
