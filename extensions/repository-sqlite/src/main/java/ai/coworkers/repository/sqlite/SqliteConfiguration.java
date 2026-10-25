package ai.coworkers.repository.sqlite;

import ai.coworkers.core.model.runtime.saga.SagaCodec;
import ai.coworkers.core.model.runtime.step.slot.StepSlotCodec;
import ai.coworkers.core.model.runtime.workflow.WorkflowCodec;
import ai.coworkers.core.model.template.step.StepTemplateCodec;
import ai.coworkers.core.model.template.step.condition.StepConditionCodec;
import ai.coworkers.core.model.template.workflow.WorkflowTemplateCodec;
import ai.coworkers.serd.json.saga.SagaJson;
import ai.coworkers.serd.json.step.StepTemplateJson;
import ai.coworkers.serd.json.workflow.WorkflowJson;
import ai.coworkers.serd.json.workflow.WorkflowTemplateJson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@ComponentScan
@Configuration(proxyBeanMethods = false)
public class SqliteConfiguration {

  /**
   * {@code spring.datasource.url}의 DB 파일 디렉터리를 생성하는 후처리기를 등록함.
   *
   * @param environment 스프링 환경
   * @return DB 파일 디렉터리 생성 후처리기
   */
  @Bean
  static SqliteDirectoryInitializer sqliteDirectoryInitializer(Environment environment) {
    return new SqliteDirectoryInitializer(environment.getProperty("spring.datasource.url"));
  }

  /**
   * 실행 모듈이 등록한 조건 코덱으로 템플릿 코덱을 등록함.
   *
   * @param conditionCodec 조건 트리 코덱
   * @return 템플릿 코덱
   */
  @Bean
  StepTemplateCodec stepTemplateCodec(StepConditionCodec conditionCodec) {
    return new StepTemplateCodec(conditionCodec);
  }

  /**
   * 템플릿 JSON 변환기를 등록함.
   *
   * @return 템플릿 JSON 변환기
   */
  @Bean
  StepTemplateJson stepTemplateJson() {
    return new StepTemplateJson();
  }

  /**
   * 워크플로우 틀 코덱을 등록함.
   *
   * @return 워크플로우 틀 코덱
   */
  @Bean
  WorkflowTemplateCodec workflowTemplateCodec() {
    return new WorkflowTemplateCodec();
  }

  /**
   * 워크플로우 틀 JSON 변환기를 등록함.
   *
   * @return 워크플로우 틀 JSON 변환기
   */
  @Bean
  WorkflowTemplateJson workflowTemplateJson() {
    return new WorkflowTemplateJson();
  }

  /**
   * 사가 코덱을 등록함.
   *
   * @return 사가 코덱
   */
  @Bean
  SagaCodec sagaCodec() {
    return new SagaCodec();
  }

  /**
   * 사가 JSON 변환기를 등록함.
   *
   * @return 사가 JSON 변환기
   */
  @Bean
  SagaJson sagaJson() {
    return new SagaJson();
  }

  /**
   * 슬롯 코덱과 사가 코덱으로 워크플로우 실행 코덱을 등록함.
   *
   * @param sagaCodec 사가 코덱
   * @return 워크플로우 실행 코덱
   */
  @Bean
  WorkflowCodec workflowCodec(SagaCodec sagaCodec) {
    return new WorkflowCodec(new StepSlotCodec(), sagaCodec);
  }

  /**
   * 워크플로우 실행 JSON 변환기를 등록함.
   *
   * @return 워크플로우 실행 JSON 변환기
   */
  @Bean
  WorkflowJson workflowJson() {
    return new WorkflowJson();
  }
}
