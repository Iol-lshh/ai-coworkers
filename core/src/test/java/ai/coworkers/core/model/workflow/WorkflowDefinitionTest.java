package ai.coworkers.core.model.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.coworkers.core.model.workflow.step.BranchStep;
import ai.coworkers.core.model.workflow.step.ForEachStep;
import ai.coworkers.core.model.workflow.step.HumanReviewStep;
import ai.coworkers.core.model.workflow.step.LoopUntilStep;
import ai.coworkers.core.model.workflow.step.StepDefinition;
import ai.coworkers.core.model.workflow.step.TaskStep;
import ai.coworkers.core.model.workflow.step.TaskType;
import ai.coworkers.core.model.workflow.step.TestTaskType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkflowDefinitionTest {

    private static TaskStep task(String key, TaskType taskType) {
        return new TaskStep(key, taskType, Map.of());
    }

    @Test
    void 하위_단계를_포함한_모든_단계를_정의_순서대로_반환함() {
        TaskStep analyze = task("analyze", TestTaskType.ANALYZE);
        HumanReviewStep review = new HumanReviewStep("review", "analyze");
        LoopUntilStep loop = new LoopUntilStep("analysis-loop", "approved", 3, List.of(analyze, review));
        TaskStep implement = task("implement", TestTaskType.IMPLEMENT);
        ForEachStep forEach = new ForEachStep("todos", "analyze", List.of(implement));

        WorkflowDefinition definition = new WorkflowDefinition("planning", 1, List.of(loop, forEach));

        assertThat(definition.allSteps()).containsExactly(loop, analyze, review, forEach, implement);
    }

    @Test
    void 분기_단계는_참_단계_다음에_거짓_단계를_반환함() {
        TaskStep fix = task("fix", TestTaskType.IMPLEMENT);
        TaskStep report = task("report", TestTaskType.ANALYZE);
        BranchStep branch = new BranchStep("quality", "has-issues", List.of(fix), List.of(report));

        WorkflowDefinition definition = new WorkflowDefinition("planning", 1, List.of(branch));

        assertThat(definition.allSteps()).containsExactly(branch, fix, report);
    }

    @Test
    void 분기_단계_안의_키가_중복되면_거부함() {
        List<StepDefinition> steps = List.of(new BranchStep("quality", "has-issues",
                List.of(task("fix", TestTaskType.IMPLEMENT)),
                List.of(task("fix", TestTaskType.ANALYZE))));

        assertThatThrownBy(() -> new WorkflowDefinition("planning", 1, steps))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate step key: fix");
    }

    @Test
    void 단계_키가_중복되면_거부함() {
        List<StepDefinition> steps = List.of(
                task("analyze", TestTaskType.ANALYZE),
                new LoopUntilStep("loop", "approved", 1, List.of(task("analyze", TestTaskType.IMPLEMENT))));

        assertThatThrownBy(() -> new WorkflowDefinition("planning", 1, steps))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate step key: analyze");
    }

    @Test
    void 리뷰_대상_단계가_없으면_거부함() {
        List<StepDefinition> steps = List.of(new HumanReviewStep("review", "missing"));

        assertThatThrownBy(() -> new WorkflowDefinition("planning", 1, steps))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown step key: missing");
    }

    @Test
    void 순회_원천_단계가_없으면_거부함() {
        List<StepDefinition> steps = List.of(
                new ForEachStep("todos", "missing", List.of(task("implement", TestTaskType.IMPLEMENT))));

        assertThatThrownBy(() -> new WorkflowDefinition("planning", 1, steps))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown step key: missing");
    }

    @Test
    void 버전이_1보다_작으면_거부함() {
        List<StepDefinition> steps = List.of(task("analyze", TestTaskType.ANALYZE));

        assertThatThrownBy(() -> new WorkflowDefinition("planning", 0, steps))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 단계가_비어_있으면_거부함() {
        assertThatThrownBy(() -> new WorkflowDefinition("planning", 1, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 반복_횟수가_1보다_작으면_거부함() {
        List<StepDefinition> body = List.of(task("analyze", TestTaskType.ANALYZE));

        assertThatThrownBy(() -> new LoopUntilStep("loop", "approved", 0, body))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 키가_비어_있으면_거부함() {
        assertThatThrownBy(() -> task(" ", TestTaskType.ANALYZE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 정의는_불변임() {
        Map<String, String> parameters = new java.util.HashMap<>(Map.of("prompt", "analyze.md"));
        TaskStep step = new TaskStep("analyze", TestTaskType.ANALYZE, parameters);
        parameters.put("prompt", "changed.md");

        assertThat(step.parameters()).containsEntry("prompt", "analyze.md");
    }
}
