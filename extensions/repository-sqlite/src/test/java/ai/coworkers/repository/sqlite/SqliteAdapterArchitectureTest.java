package ai.coworkers.repository.sqlite;

import ai.coworkers.architecture.AdapterArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = "ai.coworkers.repository.sqlite", importOptions = ImportOption.DoNotIncludeTests.class)
class SqliteAdapterArchitectureTest extends AdapterArchitectureTest {
}
