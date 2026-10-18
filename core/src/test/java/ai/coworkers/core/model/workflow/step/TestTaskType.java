package ai.coworkers.core.model.workflow.step;

public enum TestTaskType implements TaskType {
    ANALYZE("test.analyze"),
    IMPLEMENT("test.implement");

    private final String code;

    TestTaskType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
