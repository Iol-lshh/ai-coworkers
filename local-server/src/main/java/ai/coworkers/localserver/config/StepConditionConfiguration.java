package ai.coworkers.localserver.config;

import ai.coworkers.core.model.workflow.step.condition.StepConditionCodec;
import ai.coworkers.serd.json.step.StepConditionJson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 조건 트리 변환 설정. 저장소와 API가 같은 조건 종류 등록을 씀. */
@Configuration(proxyBeanMethods = false)
class StepConditionConfiguration {

  /**
   * 기본 조건 종류로 조건 코덱을 등록함.
   *
   * @return 조건 트리 코덱
   */
  @Bean
  StepConditionCodec stepConditionCodec() {
    return new StepConditionCodec();
  }

  /**
   * 조건 트리 JSON 변환기를 등록함.
   *
   * @return 조건 트리 JSON 변환기
   */
  @Bean
  StepConditionJson stepConditionJson() {
    return new StepConditionJson();
  }
}
