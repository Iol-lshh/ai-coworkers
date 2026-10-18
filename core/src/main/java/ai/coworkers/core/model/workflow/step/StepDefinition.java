package ai.coworkers.core.model.workflow.step;

/**
 * 워크플로우를 구성하는 단계 정의
 */
public sealed interface StepDefinition
        permits TaskStep, HumanReviewStep, BranchStep, LoopUntilStep, ForEachStep {

    /**
     * 워크플로우 정의 안에서 유일한 단계 키를 반환함.
     *
     * @return 단계 키
     */
    String key();
}
