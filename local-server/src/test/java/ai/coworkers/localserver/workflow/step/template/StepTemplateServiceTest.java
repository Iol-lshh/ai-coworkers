package ai.coworkers.localserver.workflow.step.template;

import static org.assertj.core.api.Assertions.assertThat;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.CreateParallel;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.FindPage;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand.Update;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateLogRepository;
import java.util.List;

import ai.coworkers.localserver.service.workflow.step.template.StepTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepTemplateService")
class StepTemplateServiceTest {

  private static final StepTemplateId JOIN = StepTemplateId.generate();
  private static final CreateParallel DEFINITION = new CreateParallel(List.of(), JOIN);

  private static final StepTemplateLogRepository NO_LOG = log -> {};

  private final MemoryTemplateRepository templates = new MemoryTemplateRepository();
  private final StepTemplateService service = new StepTemplateService(templates, NO_LOG);

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("도메인 서비스가 등록한 현재본을 반환함")
    void returnsRegistered() {
      StepTemplate created = service.create(DEFINITION);

      assertThat(created).isEqualTo(DEFINITION.toTemplate(created.id(), 1L));
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("도메인 서비스가 찾은 현재본을 반환함")
    void returnsCurrent() {
      StepTemplate created = service.create(DEFINITION);

      assertThat(service.findById(created.id())).contains(created);
    }
  }

  @Nested
  @DisplayName("page")
  class PageMethod {

    @Test
    @DisplayName("도메인 서비스가 조회한 현재본 페이지를 반환함")
    void returnsPage() {
      StepTemplate created = service.create(DEFINITION);

      assertThat(service.page(new FindPage(0, 10))).isEqualTo(new Page<>(1, List.of(created)));
    }
  }

  @Nested
  @DisplayName("update")
  class UpdateMethod {

    @Test
    @DisplayName("도메인 서비스가 수정한 현재본을 반환함")
    void returnsUpdated() {
      StepTemplate created = service.create(DEFINITION);

      assertThat(service.update(new Update(created.id(), 1L, DEFINITION)))
          .isEqualTo(DEFINITION.toTemplate(created.id(), 2L));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("도메인 서비스가 현재본을 삭제함")
    void deletesCurrent() {
      StepTemplate created = service.create(DEFINITION);

      service.delete(created.id());

      assertThat(templates.existsById(created.id())).isFalse();
    }
  }
}
