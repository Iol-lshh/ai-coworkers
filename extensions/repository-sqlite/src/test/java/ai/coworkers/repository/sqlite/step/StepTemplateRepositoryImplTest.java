package ai.coworkers.repository.sqlite.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.template.step.BranchStepTemplate;
import ai.coworkers.core.model.template.step.ParallelStepTemplate;
import ai.coworkers.core.model.template.step.StepTemplate;
import ai.coworkers.core.model.template.step.StepTemplateCodec;
import ai.coworkers.core.model.template.step.StepTemplateCommand.FindPage;
import ai.coworkers.core.model.template.step.StepTemplateId;
import ai.coworkers.core.model.template.step.condition.StepConditionCodec;
import ai.coworkers.core.model.template.step.condition.StepConditionData.LeafData;
import ai.coworkers.repository.sqlite.InMemorySqlite;
import ai.coworkers.serd.json.step.StepTemplateJson;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StepTemplateRepositoryImpl")
class StepTemplateRepositoryImplTest {

  private static final StepConditionCodec CONDITION_CODEC = new StepConditionCodec();
  private static final StepTemplateId ID = StepTemplateId.generate();
  private static final StepTemplateId NEXT = StepTemplateId.generate();

  private final StepTemplateRepositoryImpl repository =
      new StepTemplateRepositoryImpl(
          InMemorySqlite.migrated(),
          new StepTemplateCodec(CONDITION_CODEC),
          new StepTemplateJson());

  private static StepTemplateId idOf(String value) {
    return new StepTemplateId(UUID.fromString(value));
  }

  private static ParallelStepTemplate parallel(StepTemplateId id, Long version) {
    return new ParallelStepTemplate(id, version, List.of(NEXT), NEXT);
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("저장한 현재본을 조건 트리까지 복원해 반환함")
    void returnsSavedTemplate() {
      BranchStepTemplate branch =
          new BranchStepTemplate(
              ID,
              1L,
              CONDITION_CODEC.decode(
                  new LeafData("core.bool", Map.of("key", "approved", "expected", "true"))),
              NEXT,
              NEXT);
      repository.save(branch);

      assertThat(repository.findById(ID)).contains(branch);
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
      StepTemplate first = parallel(idOf("0190a000-0000-7000-8000-000000000001"), 1L);
      StepTemplate second = parallel(idOf("0190a000-0000-7000-8000-000000000002"), 1L);
      StepTemplate third = parallel(idOf("0190a000-0000-7000-8000-000000000003"), 1L);
      repository.save(third);
      repository.save(first);
      repository.save(second);

      assertThat(repository.find(new FindPage(1, 2))).isEqualTo(new Page<>(3, List.of(third)));
    }

    @Test
    @DisplayName("현재본이 없으면 빈 페이지를 반환함")
    void returnsEmptyPage() {
      assertThat(repository.find(new FindPage(0, 10))).isEqualTo(new Page<>(0, List.of()));
    }
  }

  @Nested
  @DisplayName("existsById")
  class ExistsById {

    @Test
    @DisplayName("현재본이 있으면 참, 없으면 거짓을 반환함")
    void reportsPresence() {
      repository.save(parallel(ID, 1L));

      assertThat(List.of(repository.existsById(ID), repository.existsById(NEXT)))
          .containsExactly(true, false);
    }
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("직전 버전 현재본을 다음 버전으로 갱신함")
    void updatesFromPreviousVersion() {
      repository.save(parallel(ID, 1L));
      ParallelStepTemplate revised = new ParallelStepTemplate(ID, 2L, List.of(), NEXT);

      repository.save(revised);

      assertThat(repository.findById(ID)).contains(revised);
    }

    @ParameterizedTest(name = "저장된 버전 2, 새 버전 {0}")
    @ValueSource(longs = {1, 2, 4})
    @DisplayName("저장된 현재본의 다음 버전이 아니면 바꾸지 않고 충돌로 거부함")
    void rejectsNonNextVersion(long version) {
      repository.save(parallel(ID, 1L));
      repository.save(parallel(ID, 2L));

      assertThatThrownBy(
              () -> repository.save(new ParallelStepTemplate(ID, version, List.of(), NEXT)))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith("stale template version");
      assertThat(repository.findById(ID)).contains(parallel(ID, 2L));
    }
  }

  @Nested
  @DisplayName("deleteById")
  class DeleteById {

    @Test
    @DisplayName("현재본을 삭제함")
    void deletesTemplate() {
      repository.save(parallel(ID, 1L));

      repository.deleteById(ID);

      assertThat(repository.existsById(ID)).isFalse();
    }
  }
}
