# Temporal 로컬 실행 가이드

로컬에서 Temporal dev server를 SQLite 파일 모드로 실행함.

## 1. CLI 설치

```bash
brew install temporal
```

## 2. 데이터 폴더 생성

```bash
mkdir -p ~/.ai-coworkers
```

## 3. 서버 실행

```bash
temporal server start-dev --db-filename ~/.ai-coworkers/temporal.db
```

| 항목 | 값 |
|---|---|
| gRPC (Worker·클라이언트 접속) | `localhost:7233` |
| 웹 UI | http://localhost:8233 |
| namespace | `default` |

- 실행 중인 터미널을 유지할 것.
- 종료는 `Ctrl+C`.
- `--db-filename` 생략 금지. 생략하면 메모리 모드로 실행되어 종료 시 데이터가 사라짐.
- `brew services start temporal` 사용 금지. `--db-filename` 없이 로그인 시 자동 실행됨.

## 트러블슈팅

### `brew install` 시 Xcode 라이선스 오류

```
Error: You have not agreed to the Xcode license.
```

1. 라이선스에 동의함.

   ```bash
   sudo xcodebuild -license accept
   ```

2. 동의 여부를 확인함. exit code `0`이면 완료.

   ```bash
   xcodebuild -license check
   ```

3. `brew install temporal`을 다시 실행함.
