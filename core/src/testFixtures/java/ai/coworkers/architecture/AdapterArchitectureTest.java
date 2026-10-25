package ai.coworkers.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** 어댑터 아키텍처 규칙. 하위 클래스가 {@code @AnalyzeClasses}로 검사 대상을 지정함. */
public abstract class AdapterArchitectureTest {

  /** Impl은 패키지 접근자임. */
  @ArchTest
  static final ArchRule impl_is_package_private =
      classes()
          .that()
          .haveSimpleNameEndingWith("Impl")
          .should()
          .bePackagePrivate()
          .allowEmptyShould(true);

  /** Impl은 core의 인터페이스를 구현함. */
  @ArchTest
  static final ArchRule impl_implements_core_interface =
      classes()
          .that()
          .haveSimpleNameEndingWith("Impl")
          .should()
          .implement(resideInAPackage("ai.coworkers.core.."))
          .allowEmptyShould(true);
}
