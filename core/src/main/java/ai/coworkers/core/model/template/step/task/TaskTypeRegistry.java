package ai.coworkers.core.model.template.step.task;

import static ai.coworkers.core.common.ArgumentSpecifications.NOT_BLANK;

import ai.coworkers.core.common.Specification;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** 식별 코드로 작업 종류를 찾는 등록부 */
public final class TaskTypeRegistry {

  private static final Specification<String> CODE =
      NOT_BLANK.orElseThrow(
          value -> new IllegalArgumentException("task type code must not be blank"));

  private final Map<String, TaskType> taskTypes;

  /**
   * 작업 종류를 등록함. 코드가 중복되면 거부함.
   *
   * @param taskTypes 등록할 작업 종류
   */
  public TaskTypeRegistry(Collection<? extends TaskType> taskTypes) {
    Map<String, TaskType> registered = new HashMap<>();
    for (TaskType taskType : taskTypes) {
      String code = taskType.code();
      CODE.isSatisfiedBy(code);
      if (registered.putIfAbsent(code, taskType) != null) {
        throw new IllegalArgumentException("duplicate task type code: " + code);
      }
    }
    this.taskTypes = Map.copyOf(registered);
  }

  /**
   * 코드에 해당하는 작업 종류를 반환함. 등록되지 않은 코드면 거부함.
   *
   * @param code 식별 코드
   * @return 작업 종류
   */
  public TaskType find(String code) {
    TaskType taskType = taskTypes.get(code);
    if (taskType == null) {
      throw new IllegalArgumentException("unknown task type code: " + code);
    }
    return taskType;
  }
}
