package ai.coworkers.core.common;

import java.util.function.BiFunction;

/**
 * 기준을 적용해 누적값을 갱신하는 누산기
 *
 * @param <S> 누적값 타입
 * @param <C> 정책 기준 타입
 */
@FunctionalInterface
public interface Accumulator<S, C> extends BiFunction<S, C, S> {

  /**
   * {@link BiFunction}을 누적 정책으로 변환함.
   *
   * @param function 변환할 함수
   * @param <S> 누적값 타입
   * @param <C> 정책 기준 타입
   * @return 함수에 위임하는 누적 정책
   */
  static <S, C> Accumulator<S, C> from(BiFunction<? super S, ? super C, ? extends S> function) {
    return function::apply;
  }

  /**
   * 누적값에 기준을 적용해 갱신된 누적값을 반환함.
   *
   * @param accumulated 현재 누적값
   * @param criterion 정책 기준
   * @return 갱신된 누적값
   */
  S accumulate(S accumulated, C criterion);

  /** {@link #accumulate(Object, Object)}에 위임함. */
  @Override
  default S apply(S accumulated, C criterion) {
    return accumulate(accumulated, criterion);
  }

  /**
   * 이 정책을 적용한 뒤 같은 기준으로 {@code next}를 적용하는 정책을 반환함.
   *
   * @param next 이어서 적용할 정책
   * @return 두 정책을 차례로 적용하는 누적 정책
   */
  default Accumulator<S, C> then(Accumulator<S, C> next) {
    return (accumulated, criterion) ->
        next.accumulate(accumulate(accumulated, criterion), criterion);
  }
}
