package ai.coworkers.repository.sqlite.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.workflow.WorkflowTemplate;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCodec;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCommand.FindPage;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateId;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.workflow.WorkflowTemplateJson;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("WorkflowTemplateRepositoryImpl")
class WorkflowTemplateRepositoryImplTest {

  private static final WorkflowTemplateId ID = WorkflowTemplateId.generate();
  private static final StepTemplateId START = StepTemplateId.generate();
  private static final StepTemplateId NEXT = StepTemplateId.generate();

  private final WorkflowTemplateRepositoryImpl repository =
      new WorkflowTemplateRepositoryImpl(
          InMemorySqlite.migrated(), new WorkflowTemplateCodec(), new WorkflowTemplateJson());

  private static WorkflowTemplateId idOf(String value) {
    return new WorkflowTemplateId(UUID.fromString(value));
  }

  private static WorkflowTemplate template(WorkflowTemplateId id, Long version) {
    return new WorkflowTemplate(id, version, START, List.of(START, NEXT));
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("저장한 현재본을 복원해 반환함")
    void returnsSavedTemplate() {
      repository.save(template(ID, 1L));

      assertThat(repository.findById(ID)).contains(template(ID, 1L));
    }

    @Test
    @DisplayName("현재본이 없으면 빈 값을 반환함")
    void returnsEmptyForUnknownId() {
      assertThat(repository.findById(ID)).isEmpty();
    }
  }

  @Nested
  @DisplayName("find")
  class Find {

    @Test
    @DisplayName("식별자 순서로 해당 페이지의 현재본과 전체 개수를 반환함")
    void returnsPageInIdOrder() {
      WorkflowTemplate first = template(idOf("0190a000-0000-7000-8000-000000000001"), 1L);
      WorkflowTemplate second = template(idOf("0190a000-0000-7000-8000-000000000002"), 1L);
      WorkflowTemplate third = template(idOf("0190a000-0000-7000-8000-000000000003"), 1L);
      repository.save(third);
      repository.save(first);
      repository.save(second);

      assertThat(repository.find(new FindPage(1, 2))).isEqualTo(new Page<>(3, List.of(third)));
    }
  }

  @Nested
  @DisplayName("existsById")
  class ExistsById {

    @Test
    @DisplayName("현재본이 있으면 참, 없으면 거짓을 반환함")
    void reportsPresence() {
      repository.save(template(ID, 1L));

      assertThat(
              List.of(
                  repository.existsById(ID), repository.existsById(idOf(NEXT.value().toString()))))
          .containsExactly(true, false);
    }
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("직전 버전 현재본을 다음 버전으로 갱신함")
    void updatesFromPreviousVersion() {
      repository.save(template(ID, 1L));
      WorkflowTemplate revised = new WorkflowTemplate(ID, 2L, START, List.of(START));

      repository.save(revised);

      assertThat(repository.findById(ID)).contains(revised);
    }

    @ParameterizedTest(name = "저장된 버전 2, 새 버전 {0}")
    @ValueSource(longs = {1, 2, 4})
    @DisplayName("저장된 현재본의 다음 버전이 아니면 바꾸지 않고 충돌로 거부함")
    void rejectsNonNextVersion(long version) {
      repository.save(template(ID, 1L));
      repository.save(template(ID, 2L));

      assertThatThrownBy(() -> repository.save(template(ID, version)))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith("stale workflow template version");
      assertThat(repository.findById(ID)).contains(template(ID, 2L));
    }
  }

  @Nested
  @DisplayName("deleteById")
  class DeleteById {

    @Test
    @DisplayName("현재본을 삭제함")
    void deletesTemplate() {
      repository.save(template(ID, 1L));

      repository.deleteById(ID);

      assertThat(repository.existsById(ID)).isFalse();
    }
  }
}
