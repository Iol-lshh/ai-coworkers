package ai.coworkers.core.model.workflow.step.slot;

import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.function.Function;

/**
 * 템플릿 종류 하나를 슬롯으로 조립하는 전략
 *
 * @param <T> 조립할 템플릿 타입
 */
public interface StepSlotAssembler<T extends StepTemplate> {

  /**
   * 조립할 수 있는 템플릿 타입을 반환함.
   *
   * @return 템플릿 타입
   */
  Class<T> templateType();

  /**
   * 템플릿을 슬롯으로 조립함. 템플릿의 출구는 {@code slotIdOf}로 슬롯 식별자로 바꿈.
   *
   * @param template 조립할 템플릿
   * @param id 조립할 슬롯의 식별자
   * @param slotIdOf 템플릿 식별자에 대응하는 슬롯 식별자를 찾는 함수
   * @return 조립한 슬롯
   */
  StepSlot assemble(T template, StepSlotId id, Function<StepTemplateId, StepSlotId> slotIdOf);
}
