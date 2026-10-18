package ai.coworkers.core;

import ai.coworkers.architecture.ServiceCycleArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = "ai.coworkers.core", importOptions = ImportOption.DoNotIncludeTests.class)
class CoreServiceCycleTest extends ServiceCycleArchitectureTest {
}
