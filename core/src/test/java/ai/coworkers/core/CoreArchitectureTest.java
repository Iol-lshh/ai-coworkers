package ai.coworkers.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * core 모듈 아키텍처 규칙
 */
@AnalyzeClasses(packages = "ai.coworkers.core", importOptions = ImportOption.DoNotIncludeTests.class)
class CoreArchitectureTest {

    /**
     * core는 JDK, JSpecify 애노테이션, Lombok 생성 표시 애노테이션, core 자신에만 의존함.
     */
    @ArchTest
    static final ArchRule core_depends_only_on_jdk = classes()
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "org.jspecify.annotations..", "lombok", "ai.coworkers.core..");
}
