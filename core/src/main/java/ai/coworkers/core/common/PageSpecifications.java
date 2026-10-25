package ai.coworkers.core.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 페이지 조회 요청 판정식 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageSpecifications {

  /** 페이지 번호는 0 이상이고 크기는 1 이상임. 만족하지 않으면 거부함. */
  public static final Specification<PageCommand> VALID =
      Specification.<PageCommand>from(command -> command.page() >= 0 && command.size() >= 1)
          .orElseThrow(
              command ->
                  new IllegalArgumentException(
                      "invalid page: page=" + command.page() + ", size=" + command.size()));
}
