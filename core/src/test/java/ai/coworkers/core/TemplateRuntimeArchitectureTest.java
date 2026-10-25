package ai.coworkers.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** 정의와 실행 사이의 의존 방향 규칙 */
@AnalyzeClasses(
    packages = "ai.coworkers.core",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class TemplateRuntimeArchitectureTest {

  /** 정의는 실행에 의존하지 않음. */
  @ArchTest
  static final ArchRule template_does_not_depend_on_runtime =
      noClasses()
          .that()
          .resideInAPackage("ai.coworkers.core.model.template..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("ai.coworkers.core.model.runtime..");
}
