package ai.coworkers.core.model.template.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCommand.Create;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCommand.FindPage;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCommand.Update;
import ai.coworkers.core.model.template.workflow.log.WorkflowTemplateLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WorkflowTemplateProcessor")
class WorkflowTemplateProcessorTest {

  private static final StepTemplateId START = StepTemplateId.generate();
  private static final Create DEFINITION = new Create(START, List.of(START));
  private static final Create REVISED_DEFINITION =
      new Create(START, List.of(START, StepTemplateId.generate()));

  private final Map<WorkflowTemplateId, WorkflowTemplate> templates = new LinkedHashMap<>();
  private final List<WorkflowTemplateLog> logs = new ArrayList<>();
  private final WorkflowTemplateProcessor processor =
      new WorkflowTemplateProcessor(
          templates::containsKey,
          id -> Optional.ofNullable(templates.get(id)),
          command ->
              new Page<>(
                  templates.size(),
                  templates.values().stream()
                      .skip(command.page() * command.size())
                      .limit(command.size())
                      .toList()),
          template -> templates.put(template.id(), template),
          templates::remove,
          logs::add);

  private List<WorkflowTemplate> snapshots() {
    return logs.stream().map(WorkflowTemplateLog::snapshot).toList();
  }

  @Nested
  @DisplayName("create")
  class CreateMethod {

    @Test
    @DisplayName("새 식별자와 첫 버전으로 현재본을 저장하고 로그를 추가함")
    void storesFirstVersionWithLog() {
      WorkflowTemplate created = processor.create(DEFINITION);

      assertThat(created).isEqualTo(DEFINITION.toTemplate(created.id(), 1L));
      assertThat(templates.values()).containsExactly(created);
      assertThat(snapshots()).containsExactly(created);
    }

    @Test
    @DisplayName("등록마다 다른 식별자를 발급함")
    void issuesDistinctIds() {
      WorkflowTemplate first = processor.create(DEFINITION);
      WorkflowTemplate second = processor.create(DEFINITION);

      assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    @DisplayName("로그 등록 시각을 등록 시점의 현재 시각으로 기록함")
    void recordsCurrentTime() {
      LocalDateTime before = LocalDateTime.now();
      processor.create(DEFINITION);
      LocalDateTime after = LocalDateTime.now();

      assertThat(logs.getFirst().registeredAt()).isBetween(before, after);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("현재본을 담아 반환함")
    void returnsCurrent() {
      WorkflowTemplate created = processor.create(DEFINITION);

      assertThat(processor.findById(created.id())).contains(created);
    }

    @Test
    @DisplayName("현재본이 없으면 빈 값을 반환함")
    void returnsEmptyForUnknownId() {
      assertThat(processor.findById(WorkflowTemplateId.generate())).isEmpty();
    }
  }

  @Nested
  @DisplayName("page")
  class PageMethod {

    @Test
    @DisplayName("전체 개수와 해당 페이지의 현재본을 반환함")
    void returnsTotalAndPageContent() {
      processor.create(DEFINITION);
      processor.create(DEFINITION);
      WorkflowTemplate third = processor.create(REVISED_DEFINITION);

      assertThat(processor.page(new FindPage(1, 2))).isEqualTo(new Page<>(3, List.of(third)));
    }

    @Test
    @DisplayName("페이지 조회 요청이 판정식을 만족하지 않으면 거부함")
    void rejectsInvalidPage() {
      assertThatThrownBy(() -> processor.page(new FindPage(-1, 10)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("invalid page");
    }
  }

  @Nested
  @DisplayName("update")
  class UpdateMethod {

    @Test
    @DisplayName("수정한 정의와 올린 버전으로 현재본을 갱신하고 로그를 추가함")
    void storesRevisedWithLog() {
      WorkflowTemplate created = processor.create(DEFINITION);

      WorkflowTemplate updated = processor.update(new Update(created.id(), 1L, REVISED_DEFINITION));

      assertThat(updated).isEqualTo(REVISED_DEFINITION.toTemplate(created.id(), 2L));
      assertThat(templates.values()).containsExactly(updated);
      assertThat(snapshots()).containsExactly(created, updated);
    }

    @ParameterizedTest(name = "현재본 1, 기준 버전 {0}")
    @ValueSource(longs = {0, 2})
    @DisplayName("기준 버전이 현재본과 다르면 저장하지 않고 충돌로 거부함")
    void rejectsStaleVersion(long expectedVersion) {
      WorkflowTemplate created = processor.create(DEFINITION);

      assertThatThrownBy(
              () -> processor.update(new Update(created.id(), expectedVersion, REVISED_DEFINITION)))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith("stale workflow template version");
      assertThat(templates.values()).containsExactly(created);
      assertThat(snapshots()).containsExactly(created);
    }

    @Test
    @DisplayName("현재본이 없으면 거부함")
    void rejectsUnknownId() {
      assertThatThrownBy(
              () -> processor.update(new Update(WorkflowTemplateId.generate(), 1L, DEFINITION)))
          .isInstanceOf(NoSuchElementException.class);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("현재본만 삭제하고 로그는 남김")
    void deletesCurrentKeepingLogs() {
      WorkflowTemplate created = processor.create(DEFINITION);

      processor.delete(created.id());

      assertThat(templates).isEmpty();
      assertThat(snapshots()).containsExactly(created);
    }

    @Test
    @DisplayName("현재본이 없으면 거부함")
    void rejectsUnknownId() {
      assertThatThrownBy(() -> processor.delete(WorkflowTemplateId.generate()))
          .isInstanceOf(NoSuchElementException.class);
    }
  }
}
