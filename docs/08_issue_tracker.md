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
| **#ISSUE-005** | 로직/결함 | Jackson Deserialization: 패스워드 매핑 오류 | `User.java` | ✅ Resolved | High |
| **#ISSUE-006** | 로직/결함 | 문서 검색 페이징 누락 | `DocumentRepository` | ✅ Resolved | High |
| **#ISSUE-007** | 로직/결함 | 사용자 관리 페이징 누락 | `AdminController` | ✅ Resolved | Medium |
| **#ISSUE-008** | 빌드/결함 | Docker 빌드 메모리 부족 (Error 137) | `Dockerfile` | ✅ Resolved | High |
| **#ISSUE-009** | 로직/제안 | 검색 기능 프론트/백엔드 동기화 | UI/Backend | ✅ Resolved | Medium |
| **#ISSUE-010** | 빌드/결함 | 프론트엔드 의존성 설치 오류 (ERESOLVE) | `package.json` | ✅ Resolved | High |
| **#ISSUE-011** | 스크립트/결함 | deploy.sh 에러 핸들링 미흡 | `deploy.sh` | ✅ Resolved | Medium |
| **#ISSUE-012** | UI/결함 | MainLayout 레이아웃 깨짐 현상 | `MainLayout.vue` | ✅ Resolved | Medium |
| **#ISSUE-013** | UI/결함 | 로그인 후 새로고침 시 상태 소실 | `Router/Auth` | ✅ Resolved | High |
| **#ISSUE-014** | UI/결함 | CSS Stubbing으로 인한 스타일 소실 | `vite.config.js` | ✅ Resolved | Critical |

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

### **#ISSUE-005: Jackson Deserialization: 패스워드 매핑 오류**
*   **현상**: `User.java`의 `passwordHash` 필드가 Jackson에 의해 무시되어 Admin API를 통한 유저 생성 시 패스워드 누락 발생.
*   **조치**: `password`로 필드명 변경 및 `@JsonProperty(access = Access.WRITE_ONLY)` 적용으로 해결.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-006: 문서 검색 페이징 누락**
*   **현상**: `DocumentRepository.searchDocuments`가 `List`를 반환하여 대용량 데이터 시 성능 저하 우려.
*   **조치**: `Pageable`을 적용하여 `Page` 객체를 반환하도록 수정.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-007: 사용자 관리 페이징 누락**
*   **현상**: 어드민 사용자 관리 뷰에서 전체 데이터를 한꺼번에 로드함.
*   **조치**: 백엔드 컨트롤러와 프론트엔드 연동 로직을 페이징 API 기반으로 업데이트.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-008: Docker 빌드 메모리 부족 (Error 137)**
*   **현상**: Docker 환경에서 백엔드 빌드 중 메모리 부족(OOM)으로 프로세스가 강제 종료됨.
*   **조치**: `Dockerfile` 내 Maven 실행 옵션에 `-Xmx512m`을 추가하여 메모리 사용량 제한.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-009: 검색 기능 프론트/백엔드 동기화**
*   **현상**: 프론트엔드의 일부 검색 필터가 백엔드의 페이징 엔드포인트와 일치하지 않는 문제.
*   **조치**: 모든 필터링 시나리오가 서버 측 페이징 API를 사용하도록 통일.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-010: 프론트엔드 의존성 설치 오류 (ERESOLVE)**
*   **현상**: `npm install` 시 `@pinia/testing`과의 버전 충돌로 설치 실패.
*   **조치**: 해당 패키지 버저닝 조정 및 `--legacy-peer-deps` 옵션 사용 가이드라인 수립.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-011: deploy.sh 에러 핸들링 미흡**
*   **현상**: 배포 스크립트 중간 단계에서 오류가 발생해도 무시하고 다음 단계를 진행함.
*   **조치**: 스크립트 상단에 `set -e`를 추가하여 오류 발생 시 즉시 중단되도록 개선.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-012: MainLayout 레이아웃 깨짐 현상**
*   **현상**: `MainLayout.vue`의 `v-main` 영역에 불필요한 중앙 정렬 클래스가 적용되어 UI가 좁게 뭉치는 현상.
*   **조치**: 해당 레이아웃 디자인 수정 및 깨짐 현상 해결.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-013: 로그인 후 새로고침 시 상태 소실**
*   **현상**: 인증 직후 새로고침을 하면 Pinia 스토어의 사용자 정보가 지워져 비로그인 상태로 전환됨.
*   **조치**: 라우터 가드에서 로컬 스토리지를 확인하여 프로필을 복구(Hydrate)하는 로직 추가.
*   **해결 상태**: ✅ Resolved

### **#ISSUE-014: CSS Stubbing으로 인한 스타일 소실**
*   **현상**: `vite.config.js`에 포함된 `stub-css` 플러그인이 프로덕션 빌드 시 모든 스타일을 비워버려 화면이 깨짐.
*   **조치**: 해당 플러그인을 제거하고 빌드 재수행.
*   **해결 상태**: ✅ Resolved (2026-03-08)
