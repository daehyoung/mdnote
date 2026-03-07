# 05 종합 테스트 계획 및 설계서 (Test Strategy & Plan)

## 1. 개요 (Overview)
본 문서는 MarkDown Note System의 안정성을 모니터링하고 기능 변경 시의 사이드 이펙트를 최소화하기 위한 전사적 테스트 전략을 정의합니다. 백엔드(Spring Boot 3)와 프론트엔드(Vue 3) 환경에 맞춘 단위 테스트(Unit Test), 통합 테스트(Integration Test), 그리고 E2E(End-to-End) 테스트의 지침 및 도구를 명세합니다.

---

## 2. 백엔드 테스트 전략 (Backend Testing Strategy)

### 2.1 도구 및 프레임워크 (Tools & Frameworks)
* **Testing Framework**: JUnit 5, Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`)
* **Mocking**: Mockito
* **DB Testing**: H2 DB (인메모리) 연동 또는 Testcontainers (TCR) 연동으로 PostgreSQL 환경 격리.

### 2.2 단위 테스트 (Unit Tests)
* **대상**: 비즈니스 로직(Domain Entities, `Services`, Utils)
* **전략**: 의존성 외부 연동(예: `Repository`)은 모두 Mocking 처리하여 순수 비즈니스 로직에 결함이 없는지 독립적으로 검증.
* **핵심 커버리지 목표 (Key Objectives)**:
  - `DocumentService`: 사용자 권한 여부(`publicRead`, `groupRead` 등) 평가가 올바로 분기되는지 (Success / Exception) 테스트.
  - `FileStorageService`: 파일 저장 UUID 매핑 및 삭제 로직이 정상 동작하는지 테스트.
  - `JwtUtil`: 토큰 인코딩/디코딩/만료 체크 로직 무결성 검증.

### 2.3 통합 테스트 (Integration Tests)
* **대상**: `Controller` -> `Service` -> `Repository`의 전체 Flow 및 Database I/O.
* **전략**: `@SpringBootTest` 및 `MockMvc`를 활용하여 실제 스프링 컨텍스트를 띄우고 통합 API 응답이 API 설계 문서(`docs/api.yml`)와 일치하는지 점검.
* **핵심 커버리지 목표**:
  - `DocumentController`: 쿼리 파라미터(페이징, Tag 필터링, 검색)에 따른 조합 목록 도출 시 N+1 문제 여부 모니터링 및 성능 확인.
  - `AuthFilter` 적용 통합 검증: JWT 토큰 유무, `Admin` Role의 `/api/admin/*` 엔드포인트 거부 여부 등 Security 쇠사슬 검증.

---

## 3. 프론트엔드 테스트 전략 (Frontend Testing Strategy)

### 3.1 도구 및 프레임워크 (Tools & Frameworks)
* **Unit/Component Testing**: Vitest, Vue Test Utils
* **E2E Testing**: Cypress 또는 Playwright
* **State/Routing Mocking**: Pinia testing API, Vue Router Mock.

### 3.2 컴포넌트 단위 테스트 (Component Unit Tests)
* **대상**: 개별 UI 컴포넌트(`HomeView.vue`, `DocumentDetailView.vue` 등) 내부의 렌더링 무결성 및 순수 함수형 Utils 로직.
* **전략**: Axios/Pinia를 내부적으로 Mocking(의존 객체 스터빙) 하여 Props/Emits 동작이 UI 화면(HTML 마크업)에 올바르게 그려지는지 검증.
* **핵심 커버리지 목표**:
  - 로그인 폼의 Validation(빈 값 제한) 정상 동작 여부.
  - `DocumentEditView.vue`의 Split / Edit / Preview 모드 토글링 작동 검사.

### 3.3 E2E 테스트 (End-to-End Tests)
* **대상**: 사용자의 관점에서 브라우저 플로우가 정상 작동하는지 확인.
* **전략**: 별도의 E2E Testing Database (혹은 Fixture Mock Server)에 연결한 뒤, 클릭 이벤트 체이닝을 통해 사용자 액션을 시뮬레이션함.
* **핵심 시나리오 (Key Scenarios)**:
  1. **회원가입/로그인 흐름**: 로그인 페이지 접근 -> 유효하지 않은 자격 증명 시도(Alert 확인) -> 올바른 자격 증명(Home 리다이렉션).
  2. **문서 작성 흐름**: `New Document` 클릭 -> 본문 마크다운 타건 -> 파일 첨부 버튼 클릭 및 Mock 파일 업로드 동작 모의 -> `Save` 시전 후 목록에서 표시되는지 확인.
  3. **문서 열람 권한 흐름**: 권한이 없는 계정으로 특정 문서 URL(`/documents/999`) 직접 이동 시 Permission Denied 또는 404 동작 차단 여부 검증.

---

## 4. 데이터/테스트 환경 관리 (Test Environment & Data Management)

1. **테스트 픽스처 (Test Fixtures)**: 백엔드의 Mocking 대상과 프론트엔드의 E2E Seed 데이터를 위해 JSON 스키마 기반 픽스처(Fixture)를 공유하여 데이터 포맷의 괴리를 줄입니다.
2. **CI/CD 파이프라인 연동**: 
  - (Back) `gradlew test` (또는 `mvn test`)로 각 PR시 빌드 강제.
  - (Front) `npm run test:unit`으로 컴포넌트 깨짐 방지 강제.
3. **병렬 테스트(Parallelism)**: 장기적으로 E2E 시나리오가 비대해질 때, Cypress Dashboard나 Playwright Sharding 등을 써서 테스트 구동 시간을 단축할 옵션을 설계합니다.
