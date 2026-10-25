package ai.coworkers.core.common;

import java.util.function.UnaryOperator;

/**
 * 대상을 연산해 같은 타입으로 반환하는 정규식
 *
 * @param <T> 정규화 대상 타입
 */
@FunctionalInterface
public interface Normalizer<T> extends UnaryOperator<T> {

  /**
   * {@link UnaryOperator}를 정규화 정책으로 변환함.
   *
   * @param operator 변환할 연산자
   * @param <T> 정규화 대상 타입
   * @return 연산자에 위임하는 정규화 정책
   */
  static <T> Normalizer<T> from(UnaryOperator<T> operator) {
    return operator::apply;
  }

  /**
   * 대상을 정규화함.
   *
   * @param target 정규화 대상
   * @return 정규화된 값
   */
  T normalize(T target);

  /** {@link #normalize(Object)}에 위임함. */
  @Override
  default T apply(T target) {
    return normalize(target);
  }

  /**
   * 이 정책을 적용한 뒤 {@code next}를 적용하는 정책을 반환함.
   *
   * @param next 이어서 적용할 정책
   * @return 두 정책을 차례로 적용하는 정규화 정책
   */
  default Normalizer<T> then(Normalizer<T> next) {
    return target -> next.normalize(normalize(target));
  }
}
