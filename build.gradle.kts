import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.ErrorProneOptions
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.errorprone) apply false
}

val errorproneCore = libs.errorprone.core
val nullaway = libs.nullaway

// 모든 모듈 공통: Java 25 toolchain, JUnit, JaCoCo(분기 커버리지 XML), 아키텍처 규칙(core testFixtures), NullAway(main 소스), Lombok
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "net.ltgt.errorprone")

    group = "ai.coworkers"
    version = "0.1.0-SNAPSHOT"

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    dependencies {
        // 버전은 Spring Boot BOM에 맞춘다
        "api"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testImplementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "annotationProcessor"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testAnnotationProcessor"(platform(SpringBootPlugin.BOM_COORDINATES))
        "api"("org.jspecify:jspecify")
        "errorprone"(errorproneCore)
        "errorprone"(nullaway)
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "testCompileOnly"("org.projectlombok:lombok")
        "testAnnotationProcessor"("org.projectlombok:lombok")
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
        val mainSource = name == "compileJava"
        (options as ExtensionAware).extensions.configure<ErrorProneOptions> {
            enabled.set(mainSource)
            disableAllChecks = true
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:OnlyNullMarked", "true")
            option("NullAway:JSpecifyMode", "true")
        }
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
