package ai.coworkers.core.model.workflow.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class TaskTypeRegistryTest {

    @Test
    void 등록된_코드로_작업_종류를_찾음() {
        TaskTypeRegistry registry = new TaskTypeRegistry(List.of(TestTaskType.values()));

        assertThat(registry.find("test.analyze")).isEqualTo(TestTaskType.ANALYZE);
    }

    @Test
    void 등록되지_않은_코드는_거부함() {
        TaskTypeRegistry registry = new TaskTypeRegistry(List.of(TestTaskType.values()));

        assertThatThrownBy(() -> registry.find("test.unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.unknown");
    }

    @Test
    void 코드가_중복되면_거부함() {
        TaskType duplicate = () -> "test.analyze";

        assertThatThrownBy(() -> new TaskTypeRegistry(List.of(TestTaskType.ANALYZE, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.analyze");
    }

    @Test
    void 빈_코드는_거부함() {
        TaskType blank = () -> " ";

        assertThatThrownBy(() -> new TaskTypeRegistry(List.of(blank)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
