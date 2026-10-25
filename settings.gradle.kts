plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ai-coworkers"

include(
    "core",
    "local-server",
)

// extensions/ 하위 모듈은 폴더 이름을 그대로 프로젝트 이름으로 쓴다 (:agent-claude)
file("extensions").listFiles { f -> f.resolve("build.gradle.kts").isFile }?.forEach { dir ->
    include(dir.name)
    project(":${dir.name}").projectDir = dir
}
