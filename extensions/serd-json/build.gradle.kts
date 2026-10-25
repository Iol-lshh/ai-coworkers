// 저장 형태 변환: core 중립 형태 ↔ JSON 문자열 (Jackson)
dependencies {
    api(project(":core"))
    implementation("tools.jackson.core:jackson-databind")
}
