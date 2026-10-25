package ai.coworkers.core.model.workflow.step.slot;

import ai.coworkers.core.model.workflow.step.template.StepTemplate;
import ai.coworkers.core.model.workflow.step.template.StepTemplateId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** 한 틀에 속한 템플릿들을 슬롯으로 찍어내는 팩토리 */
public final class StepSlotFactory {

  private final List<StepSlotAssembler<?>> assemblers;

  /**
   * 조립 전략을 등록함. 같은 템플릿 타입의 전략이 둘 이상이면 거부함.
   *
   * @param assemblers 등록할 조립 전략
   */
  public StepSlotFactory(Collection<? extends StepSlotAssembler<?>> assemblers) {
    Set<Class<?>> templateTypes = new HashSet<>();
    for (StepSlotAssembler<?> assembler : assemblers) {
      if (!templateTypes.add(assembler.templateType())) {
        throw new IllegalArgumentException(
            "duplicate assembler template type: " + assembler.templateType().getName());
      }
    }
    this.assemblers = List.copyOf(assemblers);
  }

  /**
   * 템플릿마다 슬롯 식별자를 발행하고, 템플릿의 출구를 슬롯 식별자로 바꿔 슬롯을 만듦. 템플릿 식별자가 중복되거나, 틀에 없는 템플릿을 가리키거나, 조립 전략이 없는
   * 템플릿이면 거부함.
   *
   * @param templates 한 틀에 속한 템플릿
   * @return 템플릿 식별자별 슬롯
   */
  public Map<StepTemplateId, StepSlot> create(Collection<? extends StepTemplate> templates) {
    Map<StepTemplateId, StepSlotId> slotIds = new HashMap<>();
    for (StepTemplate template : templates) {
      if (slotIds.putIfAbsent(template.id(), StepSlotId.generate()) != null) {
        throw new IllegalArgumentException("duplicate template id: " + template.id());
      }
    }
    Function<StepTemplateId, StepSlotId> slotIdOf =
        templateId -> {
          StepSlotId slotId = slotIds.get(templateId);
          if (slotId == null) {
            throw new IllegalArgumentException("unknown template id: " + templateId);
          }
          return slotId;
        };

    Map<StepTemplateId, StepSlot> slots = new HashMap<>();
    for (StepTemplate template : templates) {
      StepSlot slot =
          assemble(assemblerOf(template), template, slotIdOf.apply(template.id()), slotIdOf);
      slots.put(template.id(), slot);
    }
    return Map.copyOf(slots);
  }

  /** 템플릿에 맞는 전략 중 가장 구체적인 타입의 전략을 반환함. 없거나 하나로 정해지지 않으면 거부함. */
  private StepSlotAssembler<?> assemblerOf(StepTemplate template) {
    List<StepSlotAssembler<?>> candidates = new ArrayList<>();
    for (StepSlotAssembler<?> assembler : assemblers) {
      if (assembler.templateType().isInstance(template)) {
        candidates.add(assembler);
      }
    }
    List<StepSlotAssembler<?>> mostSpecific = new ArrayList<>();
    for (StepSlotAssembler<?> candidate : candidates) {
      boolean hasSubtype =
          candidates.stream()
              .anyMatch(
                  other ->
                      other != candidate
                          && candidate.templateType().isAssignableFrom(other.templateType()));
      if (!hasSubtype) {
        mostSpecific.add(candidate);
      }
    }
    if (mostSpecific.size() != 1) {
      throw new IllegalArgumentException(
          "no single assembler for template: " + template.getClass().getName());
    }
    return mostSpecific.getFirst();
  }

  private static <T extends StepTemplate> StepSlot assemble(
      StepSlotAssembler<T> assembler,
      StepTemplate template,
      StepSlotId id,
      Function<StepTemplateId, StepSlotId> slotIdOf) {
    return assembler.assemble(assembler.templateType().cast(template), id, slotIdOf);
  }
}
