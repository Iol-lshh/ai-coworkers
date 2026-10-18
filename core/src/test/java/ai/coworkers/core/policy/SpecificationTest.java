package ai.coworkers.core.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Specification")
class SpecificationTest {

    private static final Specification<Integer> POSITIVE = candidate -> candidate > 0;
    private static final Specification<Integer> EVEN = candidate -> candidate % 2 == 0;

    @Nested
    @DisplayName("isSatisfiedBy")
    class IsSatisfiedBy {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 100})
        @DisplayName("조건을 만족하면 true를 반환함")
        void returnsTrueWhenSatisfied(int candidate) {
            assertThat(POSITIVE.isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {0, -1})
        @DisplayName("조건을 만족하지 않으면 false를 반환함")
        void returnsFalseWhenNotSatisfied(int candidate) {
            assertThat(POSITIVE.isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("from")
    class From {

        private final Specification<Integer> specification = Specification.from((Integer value) -> value % 2 == 0);

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {2, 0})
        @DisplayName("Predicate가 true면 true를 반환함")
        void returnsTrueWhenPredicateTrue(int candidate) {
            assertThat(specification.isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, -1})
        @DisplayName("Predicate가 false면 false를 반환함")
        void returnsFalseWhenPredicateFalse(int candidate) {
            assertThat(specification.isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("not")
    class Not {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {0, -1})
        @DisplayName("술어가 false면 true를 반환함")
        void returnsTrueWhenPredicateFalse(int candidate) {
            assertThat(Specification.not(POSITIVE).isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 100})
        @DisplayName("술어가 true면 false를 반환함")
        void returnsFalseWhenPredicateTrue(int candidate) {
            assertThat(Specification.not(POSITIVE).isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("test")
    class TestMethod {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 0, -1})
        @DisplayName("isSatisfiedBy 결과와 같은 값을 반환함")
        void delegatesToIsSatisfiedBy(int candidate) {
            assertThat(POSITIVE.test(candidate)).isEqualTo(POSITIVE.isSatisfiedBy(candidate));
        }
    }

    @Nested
    @DisplayName("and")
    class And {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {2, 100})
        @DisplayName("두 조건을 모두 만족하면 true를 반환함")
        void returnsTrueWhenBothSatisfied(int candidate) {
            assertThat(POSITIVE.and(EVEN).isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, -2, -1})
        @DisplayName("하나라도 만족하지 않으면 false를 반환함")
        void returnsFalseWhenEitherNotSatisfied(int candidate) {
            assertThat(POSITIVE.and(EVEN).isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("or")
    class Or {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {2, 1, -2})
        @DisplayName("하나라도 만족하면 true를 반환함")
        void returnsTrueWhenEitherSatisfied(int candidate) {
            assertThat(POSITIVE.or(EVEN).isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {-1, -3})
        @DisplayName("둘 다 만족하지 않으면 false를 반환함")
        void returnsFalseWhenNeitherSatisfied(int candidate) {
            assertThat(POSITIVE.or(EVEN).isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("negate")
    class Negate {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {0, -1})
        @DisplayName("조건을 만족하지 않으면 true를 반환함")
        void returnsTrueWhenNotSatisfied(int candidate) {
            assertThat(POSITIVE.negate().isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 100})
        @DisplayName("조건을 만족하면 false를 반환함")
        void returnsFalseWhenSatisfied(int candidate) {
            assertThat(POSITIVE.negate().isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("orElseThrow")
    class OrElseThrow {

        private final Specification<Integer> specification = POSITIVE.orElseThrow(
                value -> new IllegalArgumentException("not positive: " + value));

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 2, 100})
        @DisplayName("조건을 만족하면 true를 반환함")
        void returnsTrueWhenSatisfied(int candidate) {
            assertThat(specification.isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("조건을 만족하지 않으면 판정 대상으로 만든 예외를 던짐")
        void throwsWhenNotSatisfied(int candidate) {
            assertThatThrownBy(() -> specification.isSatisfiedBy(candidate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("not positive: " + candidate);
        }
    }
}
