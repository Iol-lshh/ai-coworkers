package ai.coworkers.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 서비스 순환 방지 아키텍처 규칙. 하위 클래스가 {@code @AnalyzeClasses}로 검사 대상을 지정함.
 */
public abstract class ServiceCycleArchitectureTest {

    /**
     * Service는 다른 Service에 의존하지 않음.
     */
    @ArchTest
    static final ArchRule service_does_not_depend_on_service = noClasses()
            .that().haveSimpleNameEndingWith("Service")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Service")
            .allowEmptyShould(true);

    /**
     * Facade는 다른 Facade에 의존하지 않음.
     */
    @ArchTest
    static final ArchRule facade_does_not_depend_on_facade = noClasses()
            .that().haveSimpleNameEndingWith("Facade")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Facade")
            .allowEmptyShould(true);
}
