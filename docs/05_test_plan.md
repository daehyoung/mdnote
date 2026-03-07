# 05 종합 테스트 계획 및 설계서 (Test Strategy & Plan)

## 1. 개요 (Overview)
본 문서는 MarkDown Note System의 안정성을 보장하고 보안 및 권한 로직의 무결성을 검증하기 위한 구체적인 테스트 계획을 정의합니다. 본 계획은 표준 지침인 [.test_strategy.md](./.test_strategy.md)를 준수하며, 모든 테스트는 **요구사항 추적 매트릭스(RTM)** 및 **유스케이스(Use Case)** 시나리오와 연동됩니다.

---

## 2. 3단계 검증 및 추적성 매핑 (3-Layer Verification & Traceability)

### 2.1 1단계: 단위 테스트 (Unit Tests) - [RTM 연동]
*   **목적**: 개별 기능 및 보안 로직의 무결성을 Req ID 기반으로 검증합니다.
*   **핵심 대상 및 매핑**:
    - **[REQ-D-04] 파일 물리 보안 (Path Traversal)**: `FileStorageService`에서 `..` 시퀀스 차단 여부 검증 (Ref: #ISSUE-001).
    - **[REQ-U-01] 인증 무결성 (JWT)**: `JwtTokenProvider`에서 변조/만료 토큰의 거부 기능 검증 (Ref: #ISSUE-002).
    - **순수 로직**: 태그 프로세싱, 권한 판별 엔진 등.

### 2.2 2단계: 모듈 통합 테스트 (Integration Tests) - [UC 연동]
*   **목적**: 유스케이스 흐름상에서의 데이터 연동 및 계층 간 협업을 검증합니다.
*   **핵심 대상 및 매핑**:
    - **[UC-D-03] 문서 상세 조회 및 권한**: 사용자 부서 정보와 문서의 `groupRead/Write` 설정을 기반으로 한 권한 필터링 흐름 검증.
    - **[UC-D-02] 문서 생성 및 부서 권한 적용**: 문서 저장(`REQ-D-01`) 시 작성자의 부서 정보가 올바르게 바인딩되고 그룹 쓰기 권한이 작동하는지 확인 (Ref: #ISSUE-003).

### 2.3 3단계: 통합 사용자 시나리오 (E2E Tests) - [UC Flow 완결]
*   **목적**: 유스케이스 기술서([.usecase.md](./.usecase.md))의 시나리오 단계(Step)별로 브라우저 동작을 완결 검증합니다.
*   **핵심 시나리오**:
    1.  **로그인 및 권한 획득 흐름 ([UC-U-01])**: 로그인 폼 타건 -> 토큰 획득 -> 권한에 맞는 대시보드 진입 확인.
    2.  **전체 문서 라이프사이클 흐름 ([UC-D-02])**: 문서 작성 -> 마크다운 렌더링 -> 파일 첨부 -> 저장 -> 목록(`REQ-D-02`) 확인.
    3.  **댓글 및 소통 흐름 ([UC-D-03])**: 특정 문서 진입 -> 댓글 작성(`REQ-CM-01`) -> 실시간 반영 확인.

---

## 3. 테스트 관리 체계 (Management & Roles)

### 3.1 역할별 품질 책임
*   **Coder**: 자신이 수정한 기능과 관련된 **Req ID** 단위 테스트 통과 책임.
*   **Tester**: **UC ID**를 기반으로 한 일련의 시나리오(Basic/Alternative Flow) 테스트 통과 책임.
*   **Reviewer**: RTM 추적성 유지 여부 및 [이슈 트래커](./08_issue_tracker.md) 통합 관리 책임.

### 3.2 추적성 유지 (Traceability Maintenance)
- 테스트 코드 작성 시 클래스/메서드 수준에서 `@DisplayName` 또는 주석을 통해 `[REQ-XXX]`, `[UC-XXX]`를 명시하여 RTM과의 동기화를 유지합니다.

---
 
 ## 4. 테스트 수행 결과 (Test Execution Results)
 
 ### 4.1 Backend Integration Tests (Python Scenario Verifier)
 - **수행 도구**: Python Scenario Verifier (`tests/scenario_verifier.py`)
 - **수행 환경**: Docker (Postgres 15 + Spring Boot Backend)
 - **검증 시나리오**:
- [x] **[UC-U-01]** 로그인 및 JWT 토큰 획득 성공 (Seeded User: `test`)
- [x] **[UC-D-02]** 실제 데이터베이스 상의 문서 생성 및 ID 발급 확인
- [x] **[UC-D-03]** 생성된 문서의 상세 조회 및 검색(Query) 정합성 확인
- [x] **[UC-D-05]** 파일 업로드 및 다운로드 무결성 검증 완료
- [x] **[UC-A-01]** 관리자 사용자 관리 Life-cycle (생성, 목록, 상태변경) 검증 완료
- [x] **[UC-U-01/PWD]** 사용자 패스워드 자가 변경 및 로그인 연동 확인 
- [x] **[UC-A-01/PWD]** 관리자에 의한 사용자 패스워드 강제 초기화 및 로그인 확인
- [x] **[SECURITY]** 타인 패스워드 변경 시도 차단 (403 Forbidden) 확인
- [x] **[SECURITY]** 일반 사용자의 관리자 API 접근 차단 (403 Forbidden) 확인
- **결과**: **PASS** (2026-03-07)

### 4.2 Frontend Unit Tests (Vitest)
- **수행 도구**: Vitest (Vue Test Utils, Pinia/Vuetify Mocks)
- **검증 대상**:
    - [x] `LoginView.spec.js`: 로그인 폼 유효성 검사 및 라우팅 (REQ-U-01)
    - [x] `DocumentDetailView.spec.js`: 문서 상세 정보 렌더링 및 권한별 UI 가시성 (UC-D-03)
    - [x] `DocumentEditView.spec.js`: 문서 편집/저장 기능 및 권한 토글 (UC-D-02)
    - [x] `markdown.spec.js`: 마크다운 렌더링 및 Mermaid/Latex 확장 검증 (REQ-D-05)
- **결과**: **PASS** (2026-03-08)

### 4.3 E2E Integration Tests (Cypress)
- **수행 도구**: Cypress
- **검증 시나리오**:
    - [x] `login_flow.cy.js`: 실 브라우저 기반 로그인 및 세션 유지 (UC-U-01)
    - [x] `document_management.cy.js`: 문서 생성 -> 조회 -> 삭제 풀 사이클 (UC-D-02)
    - [x] `permissions.cy.js`: 부서/그룹 기반 권한 제어 실동작 확인 (UC-D-03)
    - [x] `comments.cy.js`: 실시간 댓글 작성 및 표시 확인 (UC-D-03)
- **결과**: **PASS** (2026-03-08)

### 4.4 Backend Unit Tests (JUnit 5)
- **수행 도구**: Spring Boot Test (MockMvc / Mockito)
- **검증 대상 (Unit/Service Logic)**:
    - [x] `JwtTokenProviderTest.java`: 토큰 생성, 파싱, 만료 검사 (REQ-U-01)
    - [x] `FileStorageServiceTest.java`: 파일 저장 로직 및 Path Traversal 방어 (REQ-D-04)
    - [x] `DocumentIntegrationTest.java`: 문서 CRUD 로직 및 DB 계층 연동 (UC-D-02)
    - [x] `DocumentServiceTest.java`: 권한 판별 및 비즈니스 로직 단위 검증
- **결과**: **PASS** (2026-03-08)

---

## 5. 부록 (Appendices)
*   **[요구사항 추적 매트릭스 (07_traceability_matrix.md)](./07_traceability_matrix.md)**: 요구사항-UC-API-UI 맵핑 정보.
*   **[이슈 트래커 (08_issue_tracker.md)](./08_issue_tracker.md)**: 발견된 결함 및 품질 개선 사항 통합 관리.
*   **[테스트 전략 지침 (.test_strategy.md)](./.test_strategy.md)**: 에이전트 협업 표준 가이드라인.
