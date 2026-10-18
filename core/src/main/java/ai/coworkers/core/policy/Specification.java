package ai.coworkers.core.policy;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 대상이 조건을 만족하는지 판정식
 *
 * @param <T> 판정 대상 타입
 */
@FunctionalInterface
public interface Specification<T> extends Predicate<T> {

    /**
     * 대상이 조건을 만족하는지 판정함.
     *
     * @param candidate 판정 대상
     * @return 조건을 만족하면 {@code true}
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * {@link Predicate}를 판정 정책으로 변환함.
     *
     * @param predicate 변환할 술어
     * @param <T>       판정 대상 타입
     * @return 술어에 위임하는 판정 정책
     */
    static <T> Specification<T> from(Predicate<? super T> predicate) {
        return predicate::test;
    }

    /**
     * {@code target}의 판정을 반전한 정책을 반환함.
     *
     * @param target 반전할 술어
     * @param <T>    판정 대상 타입
     * @return 반전된 판정 정책
     */
    static <T> Specification<T> not(Predicate<? super T> target) {
        return Specification.<T>from(target).negate();
    }

    /**
     * {@link #isSatisfiedBy(Object)}에 위임함.
     */
    @Override
    default boolean test(T candidate) {
        return isSatisfiedBy(candidate);
    }

    /**
     * 이 정책과 {@code other}를 모두 만족해야 하는 정책을 반환함.
     *
     * @param other 함께 만족해야 할 술어
     * @return 논리곱 판정 정책
     */
    @Override
    default Specification<T> and(Predicate<? super T> other) {
        return candidate -> isSatisfiedBy(candidate) && other.test(candidate);
    }

    /**
     * 이 정책과 {@code other} 중 하나를 만족하면 되는 정책을 반환함.
     *
     * @param other 대신 만족해도 되는 술어
     * @return 논리합 판정 정책
     */
    @Override
    default Specification<T> or(Predicate<? super T> other) {
        return candidate -> isSatisfiedBy(candidate) || other.test(candidate);
    }

    /**
     * 이 정책의 판정을 반전한 정책을 반환함.
     *
     * @return 반전된 판정 정책
     */
    @Override
    default Specification<T> negate() {
        return candidate -> !isSatisfiedBy(candidate);
    }

    /**
     * 이 정책을 만족하면 {@code true}를 반환하고, 만족하지 않으면 예외를 던지는 정책을 반환함.
     *
     * @param exceptionFactory 판정 대상으로 예외를 만드는 함수
     * @return 불만족 시 예외를 던지는 판정 정책
     */
    default Specification<T> orElseThrow(Function<? super T, ? extends RuntimeException> exceptionFactory) {
        return candidate -> {
            if (!isSatisfiedBy(candidate)) {
                throw exceptionFactory.apply(candidate);
            }
            return true;
        };
    }
}
