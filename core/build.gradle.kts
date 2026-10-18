// 도메인 + 포트. Spring·Temporal 등 프레임워크에 의존하지 않는다.
// testFixtures: 모든 모듈이 상속해 쓰는 아키텍처 규칙
plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesApi(libs.archunit.junit5)
}
