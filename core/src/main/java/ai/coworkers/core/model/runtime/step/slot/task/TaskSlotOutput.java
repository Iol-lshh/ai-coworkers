package ai.coworkers.core.model.runtime.step.slot.task;

import ai.coworkers.core.model.runtime.step.output.StepOutput;
import ai.coworkers.core.model.runtime.step.slot.StepSlotId;
import org.jspecify.annotations.Nullable;

/**
 * 작업 결과와 다음 슬롯을 담은 출력
 *
 * @param result 작업 결과
 * @param next 다음 슬롯. 없으면 흐름이 끝남
 */
public record TaskSlotOutput(StepOutput result, @Nullable StepSlotId next) implements StepOutput {}
