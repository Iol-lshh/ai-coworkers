# Temporal 핵심 개념

[Temporal](https://temporal.io)은 장기 실행·실패 가능한 비즈니스 로직을 코드로 작성하면 서버 장애와 무관하게 끝까지 실행을 보장하는 오케스트레이션 플랫폼임.
로컬 실행은 [Temporal 로컬 실행 가이드](temporal-local-setup.md)를 따름.

## 구성요소

| 구성요소 | 역할 |
|---|---|
| **Workflow** | 전체 흐름을 정의하는 코드. 결정적(deterministic)이어야 함. 랜덤, 현재 시각, 직접 I/O 금지 |
| **Activity** | 부수효과를 내는 작업(API 호출, DB 쓰기, CLI 실행). 실패 시 재시도 정책에 따라 자동 재시도 |
| **Worker** | Workflow·Activity 코드를 실행하는 프로세스. 직접 띄움 |
| **Temporal Server** | Event History 저장, Task Queue로 Worker에 작업 분배 |
| **Signal** | 실행 중인 Workflow에 외부 이벤트 전달 |
| **Query** | 실행 중인 Workflow 상태를 읽기 전용으로 조회 |
| **Update** | 동기 응답이 필요한 Signal. 검증 후 결과 반환 |

## Durable Execution

- Workflow 각 단계의 결과는 Event History로 서버에 기록됨.
- Worker가 죽으면 다른 Worker가 Event History를 리플레이해 직전 상태를 복원하고 이어서 실행함.
- 리플레이 결과가 같아야 하므로 Workflow 코드는 결정적이어야 함.
- `Workflow.sleep`으로 며칠~몇 달 대기를 일반 코드처럼 작성 가능함.

## 적용 대상

- 여러 서비스에 걸친 트랜잭션과 보상 로직(Saga 패턴)
- 결제·주문·온보딩 등 다단계 장기 프로세스
- 사람의 승인을 기다리는 흐름(Signal로 승인 이벤트 수신)
- 크론·큐·재시도·상태 테이블 조합의 대체
- AI 에이전트 오케스트레이션(LLM·에이전트 호출을 Activity로 감싸 재시도·상태 복구 위임)

## 장단점

| 장점 | 단점 |
|---|---|
| 재시도·타임아웃·상태 관리를 플랫폼이 담당 | Temporal Server 운영 필요(또는 Temporal Cloud) |
| 비즈니스 로직이 일반 코드처럼 읽힘 | 결정성 제약 |
| 전체 실행 이력이 남아 디버깅 용이 | 실행 중인 Workflow가 있을 때 코드 변경 시 버전 관리 필요(`Workflow.getVersion`) |

## 지원 SDK

Java, Go, TypeScript, Python, .NET, PHP, Ruby
