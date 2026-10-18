package ai.coworkers.core.policy.argument;

import ai.coworkers.core.policy.Specification;
import java.util.Collection;
import java.util.Objects;

/**
 * 도메인에 무관한 공통 판정식
 */
public final class ArgumentSpecifications {

    /**
     * {@code null}이 아님.
     */
    public static final Specification<Object> NOT_NULL = Objects::nonNull;

    /**
     * {@code null}이 아니고 공백이 아닌 문자를 포함함.
     */
    public static final Specification<String> NOT_BLANK = value -> value != null && !value.isBlank();

    /**
     * {@code null}이 아니고 원소를 하나 이상 포함함.
     */
    public static final Specification<Collection<?>> NOT_EMPTY = values -> values != null && !values.isEmpty();

    /**
     * {@code null}이 아니고 0보다 큼.
     */
    public static final Specification<Integer> POSITIVE = value -> value != null && value > 0;

    private ArgumentSpecifications() {
    }
}
