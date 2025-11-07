# 🧱 Project Structure Guide

워켓(Worket) 서버 애플리케이션은 Spring Boot 기반의 **계층형(Layered) 아키텍처**를 기반으로 설계되었습니다.
이 문서는 프로젝트의 패키지 구조와 각 계층의 역할, 그리고 DDD를 선택하지 않은 이유를 설명합니다.

---

## 📂 Project Structure

```
src/
├─ controller/     # 요청 진입점 (REST API, Request/Response DTO 매핑)
├─ service/        # 핵심 비즈니스 로직 처리
├─ repository/     # 데이터 접근 계층 (JPA 인터페이스 또는 구현체)
├─ domain/         # 엔티티(Entity) 및 도메인 객체 정의
├─ dto/            # 요청/응답 데이터 전송 객체
└─ global/         # 전역 공통 모듈 (config, exception, response 등)
├─ config/         # Spring 설정 및 환경 관련 Bean
├─ exception/      # 전역 예외 처리 및 커스텀 예외 정의
└─ common/         # 공통 유틸리티, 상속 엔티티 등
```

---

## 🧩 계층별 책임 (Layer Responsibility)

| 계층             | 역할 |
|----------------|------|
| **Controller** | API 요청을 수신하고 DTO로 데이터를 변환하여 Service 계층에 전달합니다. 응답 또한 DTO 형태로 반환합니다. |
| **Service**    | 비즈니스 로직을 구현하며, 트랜잭션 단위를 관리합니다. Repository 계층을 호출하여 데이터 조작을 수행합니다. |
| **Repository** | 데이터베이스 접근을 담당하며, JPA 인터페이스 또는 QueryDSL 기반 구현체를 포함합니다. |
| **Domain**     | 비즈니스 핵심 모델을 정의하며, JPA 엔티티 및 VO(Value Object)를 포함합니다. |
| **Dto**        | Controller와 Service 간 데이터 교환을 담당합니다. Entity와 1:1 매핑되지 않을 수도 있습니다. |
| **Global**     | 전역 설정, 공통 유틸리티, 예외 처리, 응답 포맷 등 프로젝트 전반에서 재사용되는 구성요소를 관리합니다. |

---

## 🧭 DDD와의 차이 및 선택 이유

### 💡 DDD (Domain-Driven Design)
DDD는 **복잡한 도메인 모델링**과 **도메인 중심의 코드 구조화**를 목표로 하는 설계 방식입니다.
보통 다음과 같은 패키지 구조를 가집니다.

```
com.worket.domain.contract
├─ Contract.java
├─ ContractRepository.java
├─ ContractService.java
├─ ContractController.java
```

즉, **도메인(계약, 거래, 사용자 등)** 단위로 코드를 묶는 형태이며,
도메인마다 자체적인 계층 구조를 포함합니다.

---

### ⚙️ 워켓의 선택: 계층형 구조 (Layered Architecture)

워켓 프로젝트는 **6인 협업 팀 환경**에서 동시에 여러 기능을 개발하기 때문에,
초기 단계에서는 **직관적이고 명확한 계층형 구조**를 선택했습니다.

#### ✅ 선택 이유
1. **역할이 명확**: 각 계층의 책임(controller, service, repository)이 분리되어 협업 시 충돌 최소화
2. **진입 장벽이 낮음**: 새 팀원이 바로 구조를 이해하고 코드 위치를 찾기 쉬움
3. **작은 규모의 프로젝트에 적합**: MVP 단계에서는 도메인 간 복잡한 의존성이 적어 DDD의 이점을 살리기 어려움
4. **확장 용이**: 추후 특정 도메인이 커질 경우, 해당 영역만 DDD 스타일로 분리 전환 가능

#### ⚠️ DDD를 적용하지 않은 이유
- 팀원 간 도메인 경계(예: 계약, 거래, 계정)의 정의가 아직 확립되지 않음
- 프로젝트 초기 단계에서 DDD의 도입은 개발 속도를 저하시킬 우려 있음
- 현재 목적은 “BaaS형 계좌이체 및 계약 정보 연동” MVP를 빠르게 검증하는 것

---

## 🔄 향후 확장 방향

향후 계정계, PG계, 워켓 간 서비스 경계가 명확해지면
다음과 같은 형태로 **도메인 단위 패키징(DDD 전환)** 을 고려할 수 있습니다.

```
com.worket.domain.contract
├─ controller/
├─ service/
├─ repository/
├─ domain/
└─ dto/
```

이때, 현재 구조의 각 계층은 그대로 유지되며,
단지 “도메인 단위로 묶는 방식”으로 확장될 예정입니다.

---

## 🧾 Summary

| 항목 | 선택 |
|------|------|
| 설계 방식 | Layered Architecture |
| 이유 | 협업 단순화, 구조 명확성, MVP 개발 속도 |
| 전환 가능성 | 추후 도메인 단위 확장 시 DDD 일부 도입 가능 |
| 전역 관리 | `/global` 패키지로 통합 관리 |

---

> 📘 본 문서는 워켓 서버 프로젝트의 코드 구조 표준을 정의하며,
> 신규 개발자는 본 문서를 참고하여 패키지 및 계층 위치를 일관성 있게 유지해야 합니다.
