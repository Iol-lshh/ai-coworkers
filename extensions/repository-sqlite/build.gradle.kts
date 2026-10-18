// Repository 포트 구현: SQLite + JdbcClient (로컬 전용)
dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.xerial:sqlite-jdbc")
}
