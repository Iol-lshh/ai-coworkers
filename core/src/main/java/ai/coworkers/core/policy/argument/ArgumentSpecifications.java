package ai.coworkers.core.policy.argument;

import ai.coworkers.core.policy.Specification;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 도메인에 무관한 공통 판정식
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgumentSpecifications {

    /**
     * 공백이 아닌 문자를 포함함.
     */
    public static final Specification<String> NOT_BLANK = value -> !value.isBlank();

    /**
     * 원소를 하나 이상 포함함.
     */
    public static final Specification<Collection<?>> NOT_EMPTY = values -> !values.isEmpty();

    /**
     * 0보다 큼.
     */
    public static final Specification<Integer> POSITIVE = value -> value > 0;
}
