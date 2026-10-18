# ai-coworkers

기획 문서를 받아 **분석 → 계획 → 작업 → 품질 보완**까지 AI 에이전트(Claude Code)가 수행하고,
단계마다 사람이 리뷰·승인하는 개발 프로세스를 워크플로우 엔진으로 오케스트레이션하는 프로젝트.

> 현재 상태: 설계 단계

## 프로세스

1. 분석 리포트 작성
2. 사용자 리뷰 → 피드백 반영 (승인까지 반복)
3. todo 계획 작성
4. 사용자 리뷰 → 피드백 반영 (승인까지 반복)
5. todo 스텝 단위 개발
6. 작업 결과 리포트 작성
7. 사용자 리뷰 (리포트 + 결과물) → 피드백 반영
8. Sonar 이슈·컨디션 커버리지 보완

## 구성

```
[데스크톱 앱] ──REST·SSE──▶ [local-server] ──▶ 워크플로우 엔진 (Temporal 등, 교체 가능)
                                  │
                                  └─▶ 에이전트 CLI (claude -p, 기획 건별 git worktree)
```

## 모듈

| 모듈                             | 역할 |
|--------------------------------|---|
| `core`                         | 도메인 모델 + 포트. 프레임워크 비의존 |
| `local-server`                 | 로컬 백엔드: REST API + 워크플로우 실행 (Spring Boot) |
| `extensions/agent-claude`      | `AgentPort` 구현: Claude Code CLI |
| `extensions/repository-sqlite` | Repository 포트 구현: SQLite + JdbcClient (로컬 전용) |
