package ai.coworkers.core.model.runtime.saga;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * 사가를 JDK 타입만으로 나타낸 저장 형태
 *
 * @param id 식별 값
 * @param state 사가 상태 이름
 * @param node 현재 노드의 저장 형태. 흐름이 끝났으면 없음
 */
public record SagaData(UUID id, String state, @Nullable NodeData node) {

  /**
   * 노드의 저장 형태. 슬롯은 식별 값으로만 가리킴.
   *
   * @param id 식별 값
   * @param state 노드 상태 이름
   * @param slotId 실행하는 슬롯 식별 값
   */
  public record NodeData(UUID id, String state, UUID slotId) {}
}
