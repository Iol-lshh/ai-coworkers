import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.spring.boot) apply false
}

// 모든 모듈 공통: Java 25 toolchain, JUnit, JaCoCo(분기 커버리지 XML), 아키텍처 규칙(core testFixtures)
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    group = "ai.coworkers"
    version = "0.1.0-SNAPSHOT"

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    dependencies {
        // 버전은 Spring Boot BOM에 맞춘다
        "implementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testImplementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.assertj:assertj-core")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        if (path != ":core") {
            "testImplementation"(testFixtures(project(":core")))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required = true
        }
    }
}
