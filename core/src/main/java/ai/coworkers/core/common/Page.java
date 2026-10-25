package ai.coworkers.core.common;

import java.util.List;

public record Page<T>(long total, List<T> list) {}
