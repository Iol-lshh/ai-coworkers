package ai.coworkers.repository.sqlite;

import ai.coworkers.core.model.workflow.step.condition.StepConditionCodec;
import ai.coworkers.core.model.workflow.step.template.StepTemplateCodec;
import ai.coworkers.serd.json.step.StepTemplateJson;
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
}
