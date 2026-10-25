package ai.coworkers.localserver;

import ai.coworkers.architecture.ServiceCycleArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(
    packages = "ai.coworkers.localserver",
    importOptions = ImportOption.DoNotIncludeTests.class)
class LocalServerServiceCycleTest extends ServiceCycleArchitectureTest {}
