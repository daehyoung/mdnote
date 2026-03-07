# 08 이슈 트래커 (Issue Tracker)

본 문서는 시스템 개발 및 리뷰 과정에서 발견된 결함(Bug), 설계 결함(Design Flaw), 그리고 개선 제안사항을 통합 관리하는 문서입니다. 각 이슈는 고유한 ID를 부여받아 해결 상태가 추적됩니다.

---

## 🛠 이슈 요약 (Issue Summary)

| ID | 유형 | 제목 | 관련 모듈 | 상태 | 우선순위 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **#ISSUE-001** | 보안/결함 | FileStorageService 경로 탐색(Path Traversal) 취약점 | `FileStorageService` | ✅ Resolved | High |
| **#ISSUE-002** | 보안/결함 | JWT 토큰 변조 검증 누락 | `JwtTokenProvider` | ✅ Resolved | High |
| **#ISSUE-003** | 로직/결함 | 부서별(Group) 쓰기 권한 검증 테스트 누락 | `DocumentService` | ✅ Resolved | Medium |
| **#ISSUE-004** | 성능/제안 | 태그 프로세싱(@processTags) DB IO 최적화 | `DocumentService` | ⏳ Open | Low |

---

## 📋 이슈 상세 내역

### **#ISSUE-001: FileStorageService 경로 탐색 취약점**
*   **발견일**: 2026-03-07
*   **유형**: 보안 결함 (Logic Bug)
*   **현상**: 파일 저장 시 보안 체크(`..` 포함 여부)를 원본 파일명이 아닌 생성된 UUID 파일명에 대해 수행하여 체크 로직이 무력화됨.
*   **조치**: `originalFileName`을 검증하도록 로직 수정 및 `testStoreFileWithTraversal` 테스트 케이스 추가 완료.
*   **해결 상태**: ✅ Resolved (2026-03-07)

### **#ISSUE-002: JWT 토큰 변조 검증 누락**
*   **발견일**: 2026-03-07
*   **유형**: 보안 결함 (Security Gap)
*   **현상**: `validateToken` 로직에서 토큰 서명이 변조된 경우에 대한 단위 테스트가 부재함.
*   **조치**: `JwtTokenProviderTest`에 `testTamperedToken` 케이스를 추가하여 변조 토큰 감지 기능 확인.
*   **해결 상태**: ✅ Resolved (2026-03-07)

### **#ISSUE-003: 부서별(Group) 쓰기 권한 검증 테스트 누락**
*   **발견일**: 2026-03-07
*   **유형**: 로직 결함 (Requirement Gap)
*   **현상**: 유스케이스에는 정의되어 있으나, 동일 부서원 간의 그룹 쓰기 권한이 작동하는지 확인하는 자동화 테스트가 누락됨.
*   **조치**: `DocumentServiceTest`에 `testUpdateDocumentGroupPermission` 시나리오 추가.
*   **해결 상태**: ✅ Resolved (2026-03-07)

### **#ISSUE-004: 태그 프로세싱 DB IO 최적화 제안**
*   **발견일**: 2026-03-07
*   **유형**: 성능 개선 (Optimization)
*   **현상**: `processTags` 수행 시 각 태그마다 개별적인 조회가 발생함. 고빈도 호출 시 DB 부하 가능성.
*   **제안**: `findByNameIn` 등의 Batch 조회를 이용한 리팩토링 권고.
*   **해결 상태**: ⏳ Open (Proposed)
| ISSUE-004 | Jackson Deserialization: User password mapping failure | High | `User.java`의 `passwordHash` 필드가 Jackson에 의해 무시되어 Admin API를 통한 유저 생성 시 패스워드 누락 발생. `password`로 필드명 변경 및 `@JsonProperty(access = Access.WRITE_ONLY)` 적용으로 해결. |
