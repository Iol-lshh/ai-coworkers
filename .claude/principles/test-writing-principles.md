# 테스트 작성 원칙

- 테스트 클래스는 대상과 같은 패키지에 `{대상 클래스명}Test`로 작성할 것.
- 테스트 클래스에 대상 클래스명으로 `@DisplayName`을 붙일 것.
- 대상 메서드마다 `@Nested` 이너 클래스를 하나씩 둘 것.
- 이너 클래스명은 메서드명을 PascalCase로 쓰고, JUnit 타입명과 겹치면 `Method` 접미사를 붙일 것.
- 이너 클래스에 메서드명으로 `@DisplayName`을 붙일 것.
- 테스트 메서드의 `@DisplayName`은 검증하는 동작을 음슴체로 작성할 것.
- 입력만 다른 상황은 `@ParameterizedTest` 하나로 묶을 것.
- 기대값이 있으면 `@CsvSource`, 입력만 있으면 `@ValueSource`로 상황을 제공할 것.
- `@ParameterizedTest`의 `name`에 입력과 기대값을 표시할 것.
- 검증은 AssertJ `assertThat`, `assertThatThrownBy`로 작성할 것.
- 같은 대상에 대한 검증은 하나의 assertion 체인으로 이어 쓸 것.
- `isEqualTo`는 같은 의미의 AssertJ 전용 메서드가 없을 때만 쓸 것.
- 적용 순서가 있는 메서드는 결과와 순서를 모두 검증할 것.
- 인터페이스 테스트용 구현은 테스트 클래스 안에 람다 상수로 정의할 것.
