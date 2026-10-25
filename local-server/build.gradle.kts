// 로컬 백엔드: REST API + Temporal Worker (UI는 desktop에서)
plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":repository-sqlite"))
    implementation(project(":serd-json"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-tx")
    implementation(libs.springdoc.webmvc.ui)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-jdbc")
}
