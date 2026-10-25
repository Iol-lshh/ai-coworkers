package ai.coworkers.localserver.workflow.step.template;

import ai.coworkers.core.common.Page;
import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCommand;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import ai.coworkers.core.model.workflow.step.template.StepTemplateRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 등록 순서를 유지하는 테스트용 템플릿 현재본 저장소 */
class MemoryTemplateRepository implements StepTemplateRepository {

  private final Map<StepTemplateId, StepTemplate> store = new LinkedHashMap<>();

  @Override
  public Optional<StepTemplate> findById(StepTemplateId id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Page<StepTemplate> find(StepTemplateCommand.FindPage command) {
    List<StepTemplate> content =
        store.values().stream()
            .skip(command.page() * command.size())
            .limit(command.size())
            .toList();
    return new Page<>(store.size(), content);
  }

  @Override
  public boolean existsById(StepTemplateId id) {
    return store.containsKey(id);
  }

  @Override
  public void save(StepTemplate template) {
    store.put(template.id(), template);
  }

  @Override
  public void deleteById(StepTemplateId id) {
    store.remove(id);
  }
}
