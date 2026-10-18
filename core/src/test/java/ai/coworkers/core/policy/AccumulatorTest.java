package ai.coworkers.core.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Accumulator")
class AccumulatorTest {

    private static final Accumulator<Integer, Integer> ADD = Integer::sum;
    private static final Accumulator<Integer, Integer> MULTIPLY = (accumulated, criterion) -> accumulated * criterion;

    @Nested
    @DisplayName("accumulate")
    class Accumulate {

        @ParameterizedTest(name = "누적값 {0}, 기준 {1} → {2}")
        @CsvSource({
                "0, 1, 1",
                "5, 3, 8",
                "5, -3, 2",
        })
        @DisplayName("누적값에 기준을 적용한 결과를 반환함")
        void returnsAccumulated(int accumulated, int criterion, int expected) {
            assertThat(ADD.accumulate(accumulated, criterion)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("from")
    class From {

        @ParameterizedTest(name = "누적값 {0}, 기준 {1} → {2}")
        @CsvSource({
                "2, 3, 6",
                "0, 3, 0",
                "-2, 3, -6",
        })
        @DisplayName("BiFunction에 위임하는 누적 정책을 생성함")
        void delegatesToFunction(int accumulated, int criterion, int expected) {
            Accumulator<Integer, Integer> accumulator = Accumulator.from((Integer a, Integer c) -> a * c);

            assertThat(accumulator.accumulate(accumulated, criterion)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @ParameterizedTest(name = "누적값 {0}, 기준 {1}")
        @CsvSource({
                "0, 1",
                "5, 3",
                "5, -3",
        })
        @DisplayName("accumulate 결과와 같은 값을 반환함")
        void delegatesToAccumulate(int accumulated, int criterion) {
            assertThat(ADD.apply(accumulated, criterion)).isEqualTo(ADD.accumulate(accumulated, criterion));
        }
    }

    @Nested
    @DisplayName("then")
    class Then {

        @ParameterizedTest(name = "누적값 {0}, 기준 {1} → {2}")
        @CsvSource({
                "1, 2, 6",
                "0, 3, 9",
                "-1, 1, 0",
        })
        @DisplayName("이 정책을 먼저 적용한 뒤 같은 기준으로 next를 적용함")
        void appliesInOrderWithSameCriterion(int accumulated, int criterion, int expected) {
            assertThat(ADD.then(MULTIPLY).accumulate(accumulated, criterion)).isEqualTo(expected);
        }
    }
}
