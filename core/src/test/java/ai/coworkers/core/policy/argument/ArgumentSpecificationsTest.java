package ai.coworkers.core.policy.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ArgumentSpecifications")
class ArgumentSpecificationsTest {

    @Nested
    @DisplayName("NOT_NULL")
    class NotNull {

        @Test
        @DisplayName("null이 아니면 만족함")
        void satisfiedByNonNull() {
            assertThat(ArgumentSpecifications.NOT_NULL.isSatisfiedBy("value")).isTrue();
        }

        @Test
        @DisplayName("null이면 만족하지 않음")
        void notSatisfiedByNull() {
            assertThat(ArgumentSpecifications.NOT_NULL.isSatisfiedBy(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("NOT_BLANK")
    class NotBlank {

        @ParameterizedTest(name = "[{0}]")
        @ValueSource(strings = {"a", " a ", "analyze"})
        @DisplayName("공백이 아닌 문자를 포함하면 만족함")
        void satisfiedByText(String candidate) {
            assertThat(ArgumentSpecifications.NOT_BLANK.isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "[{0}]")
        @NullSource
        @ValueSource(strings = {"", " ", "\t\n"})
        @DisplayName("null이거나 공백뿐이면 만족하지 않음")
        void notSatisfiedByBlank(String candidate) {
            assertThat(ArgumentSpecifications.NOT_BLANK.isSatisfiedBy(candidate)).isFalse();
        }
    }

    @Nested
    @DisplayName("NOT_EMPTY")
    class NotEmpty {

        @Test
        @DisplayName("원소가 있으면 만족함")
        void satisfiedByNonEmpty() {
            assertThat(ArgumentSpecifications.NOT_EMPTY.isSatisfiedBy(List.of("a"))).isTrue();
        }

        @Test
        @DisplayName("비어 있으면 만족하지 않음")
        void notSatisfiedByEmpty() {
            assertThat(ArgumentSpecifications.NOT_EMPTY.isSatisfiedBy(List.of())).isFalse();
        }

        @Test
        @DisplayName("null이면 만족하지 않음")
        void notSatisfiedByNull() {
            assertThat(ArgumentSpecifications.NOT_EMPTY.isSatisfiedBy(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("POSITIVE")
    class Positive {

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {1, 2, Integer.MAX_VALUE})
        @DisplayName("0보다 크면 만족함")
        void satisfiedByPositive(int candidate) {
            assertThat(ArgumentSpecifications.POSITIVE.isSatisfiedBy(candidate)).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        @DisplayName("0 이하면 만족하지 않음")
        void notSatisfiedByNonPositive(int candidate) {
            assertThat(ArgumentSpecifications.POSITIVE.isSatisfiedBy(candidate)).isFalse();
        }

        @Test
        @DisplayName("null이면 만족하지 않음")
        void notSatisfiedByNull() {
            assertThat(ArgumentSpecifications.POSITIVE.isSatisfiedBy(null)).isFalse();
        }
    }
}
