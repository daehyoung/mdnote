# 07 요구사항 추적 매트릭스 (Requirement Traceability Matrix)

## 1. 개요 (Overview)
본 문서는 MarkDown Note System 프로젝트의 요구사항(Requirements) 시작점부터 최종 구현된 화면(UI) 및 백엔드 인터페이스(API)까지의 흐름을 양방향으로 추적 가능하도록 정의한 RTM(Requirement Traceability Matrix)입니다.
결함 추적(Defect Tracking) 및 형상 변경에 따른 영향도 분석 시 참조 기준으로 사용합니다.

## 2. 매트릭스 (Traceability Matrix)

| Req ID | 요구사항 명(기능) | Use Case ID | Frontend 화면 뷰 | 주 사용 요소(UI ID) | Backend API | Backend 모듈 (02_feature) | 비고 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-U-01** | 사용자 로그인 | `UC-U-01` | `LoginView.vue` | `#btn-login` | `POST /api/v1/auth/login` | `AuthController.login()` | JWT 토큰 발급 |
| **REQ-U-02** | 내 정보 관리 | `UC-U-02` | `ProfileView.vue` | `#btn-profile` | `PUT /api/v1/users/me` | `ProfileController` | 회원 정보 수정 |
| **REQ-U-03** | 사용자 관리 (어드민) | `UC-A-01` | `UserManageView.vue` | 툴바, `#btn-add-user` | `POST /api/v1/users` | `AdminController` | Admin Role 권한 필수 |
| **REQ-U-04** | 조직/부서 관리 | `UC-A-04` | `OrganizationView.vue` | 트리 아이템 | `POST/PUT /api/v1/departments` | `DepartmentController` | 시스템 조직 계층도 관리 |
| **REQ-D-01** | 문서 생성/저장 | `UC-D-02` | `DocumentEditView.vue` | `#btn-save`, Markdown 에디터 | `POST /api/v1/documents` <br> `PUT /api/v1/documents/{id}` | `DocumentController` <br> (`create`/`update`) | Status(DRAFT 등) 동시 제어 |
| **REQ-D-01** | 문서 삭제 | `UC-D-02` | `DocumentEditView.vue` | `#btn-del-doc` | `DELETE /api/v1/documents/{id}` | `DocumentController.delete()` | 본인 작성 문서 혹은 Admin |
| **REQ-D-02** | 문서 목록 뷰어 | `UC-D-01` | `HomeView.vue` | `#list-item-doc`, `#pagination` | `GET /api/v1/documents` | `DocumentController.getAll...` | PUBLISHED 문서 조회 중심 |
| **REQ-D-02** | 목록 검색/태그 필터 | `UC-D-04` | `HomeView.vue` | `#input-search`, `#sel-status` | `GET /api/v1/documents?search=...` | `DocumentController` | Query Parameter 전송 |
| **REQ-D-03** | 문서 접근 권한 제어 | `UC-D-03` | `DocumentDetailView.vue` | 퍼미션 아이콘(Badge) | `GET /api/v1/documents/{id}` | `DocumentService` (권한 로직) | 백엔드 권한 필터 검증 |
| **REQ-D-04** | 첨부 파일 스토리지 | `UC-D-02` | `DocumentEditView.vue` <br> (Settings Drawer) | `#btn-attach` | `POST /api/v1/attachments/upload` | `AttachmentController` | 멀티파트 파일 업로드 |
| **REQ-C-01/02** | 카테고리 관리 | `UC-A-03` | `CategoryManageView.vue` | 트리 아이템, CRUD 팝업 | `POST/PUT /api/v1/categories` | `CategoryController` | 시스템 공통 카테고리 트리 |
| **REQ-CM-01** | 문서 댓글 대화 | `UC-D-03` | `DocumentDetailView.vue` | `#input-comment`, `#btn-post-comment` | `POST /api/v1/comments` | `CommentController` | `Allow Comments` 설정 연동 |

---

## 3. 문서 간 참조 구조 (Cross-Reference Map)
역공학을 기반으로 생성된 산출물들은 위 RTM 표를 매개로 다음과 같이 연결되어 있습니다:

- `01_requirements.md` (What to build?) ➔ `Req ID` 부여.
- `usecases.md` (How users interact?) ➔ `UC ID` 부여.
- `04_frontend_ui_manual.md` (Where users click?) ➔ `UI ID (#btn-xxx)` 부여 및 API 매핑.
- `06_deployment_plan.md` & `api.yml` (How backend serves?) ➔ `Backend API` 엔드포인트 매핑.
- `02_feature_analysis.md` (How backend logic works?) ➔ `Backend 모듈 (Controller/Service)` 매핑.
