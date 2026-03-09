# 02 기능 분석표 (Feature Analysis)

## 개요
이 문서는 MarkDown Note System 백엔드 모듈이 제공하는 실제 API 및 로직을 기반으로 각 세부 기능과 역할을 분석한 표이다.

## 기능 단위 분석표

| Req ID | 대분류 | 중분류 | 기능명 | API Path | Description (상세 기능 및 설명) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-U-01** | **User & Auth** | 인증 | 로그인 (`/login`) | `/api/v1/auth/login` | 사용자 계정/비밀번호 기반 인증. 성공 시 JWT / Simple Token 발급. |
| **REQ-U-02** | | 프로필 관리 | 내 정보 수정 | `/api/profile` | 사용자 본인의 비밀번호, 테마 변경 및 조회. (상황에 따라 UserController 연동) |
| **REQ-U-03** | | 관리자 권한 | 전체 사용자 조회 (`/users`) | `/api/admin/users` | 등록된 전체 사용자의 목록 및 상태 조회. |
| **REQ-U-03** | | | 사용자 생성/삭제 | `/api/admin/users` | 관리자가 새 사용자를 등록하거나 강제 삭제. |
| **REQ-U-03** | | | 상태 및 부서 변경 | `/api/admin/users/{id}/status` | 관리자가 사용자의 상태(ACTIVE/INACTIVE)와 부서(Department)를 할당. |
| **REQ-U-03** | | | 비밀번호 강제 초기화 | `/api/admin/users/{id}/password` | 관리자가 특정 사용자의 비밀번호를 초기화. |
| **REQ-D-02** | **Document** | 문서 기본 | 문서 목록 조회 (VIEW) | `/api/documents` | 발행 완료(PUBLISHED) 문서를 중심으로 한 지식 소비용 목록 제공. |
| **REQ-D-06** | | | 작업 대상 조회 (EDIT) | `/api/documents` | 작성자/그룹 권한 기반의 지식 관리/생산용 목록(초안 포함) 제공. |
| **REQ-D-02** | | | 문서 상세 조회 (`/{id}`) | `/api/documents/{id}` | 요청한 특정 문서의 상세 내용 응답. |
| **REQ-D-01** | | | 문서 작성/수정/삭제 | `/api/documents` | 로그인 사용자의 문서 Markdown 내용과 상태(Draft, Published)를 DB에 반영. |
| **REQ-D-03** | | 권한 제어 | 권한 검증 기능 (Service) | N/A | DB의 `publicRead`, `groupRead` 플래그 및 작성자 비교 로직을 통해 인가된 사용자만 열람 및 수정이 가능하도록 통제. |
| **REQ-C-01** | **Metadata** | 분류 | 카테고리 계층형 관리 | `/api/categories` | 트리(Parent-Child) 구조를 띈 카테고리를 생성, 조회, 수정하는 엔드포인트 제공. |
| **REQ-C-03** | | 확장성 | 태그 연동 | `/api/tags` | 문서를 생성/수정할 때 연관된 태그(Tag) 묶음을 맵핑할 수 있는 구조 제공. |
| **REQ-CM-01** | **Communication** | 피드백 | 문서 댓글 기능 (`/comments`) | `/api/documents/{docId}/comments` | 문서 상세 페이지에 하위 댓글을 작성, 삭제, 조회하는 기능. |
| **REQ-D-04** | **Storage** | 파일 처리 | 파일 업로드 (`/upload`) | `/api/attachments/upload` | MultipartFile을 받아 시스템 물리 스토리지에 UUID 형태 파일로 저장 후 메타정보 반환. |
| **REQ-D-04** | | | 첨부파일 다운로드 (`/{id}`) | `/api/attachments/{id}` | 파일 ID 기반으로 물리 스토리지에서 리소스를 로드, 본래 Logical Name으로 다운로드 제공. |
| **REQ-D-04** | | | 파일 삭제 | `/api/attachments/{id}` | 데이터베이스에서 첨부 내역과 함께 연관된 물리 파일을 삭제(향후 반영 구역). |
| **REQ-A-01** | **System Admin** | 모니터링 | 대시보드 조회 | `/api/admin/metrics` (예정) | 시스템 리소스 사용량 및 지식 생산 지표 통계 시각화 데이터 제공. |
| **REQ-A-02** | | 자원 최적화 | 고립된 파일 관리 | `/api/admin/attachments/orphaned` | `doc_id`가 없는 고립된 첨부파일 식별 및 일괄 삭제 기능. |

## 핵심 분석 및 위험 요소 (Risk Assessment)
1. **페이징 및 검색 부하**: `DocumentController.getAllDocuments()`의 검색 조건이 복잡해질 경우(카테고리+태그+전문 쿼리 등 조합 시), DB 조인 부하(N+1 Query 등)가 발생할 가능성이 있으므로 JPA Fetch Join / QueryDSL 적용이 권장됨.
2. **권한 로직 중첩**: 현재 권한 평가는 Controller가 아닌 Service 내부(예: `DocumentService.getAllDocuments`)에서 직접 콜백 User 등을 검사해 쿼리단에서 필터링하는 방식으로 구현되어 있음.
3. **파일 I/O 블로킹**: 첨부파일 로드 시 동기 IO를 사용하므로 대용량 병렬 요청 시 스레드 풀 소진 가능성이 존재함. 향후 S3 연동 또는 비동기 처리를 고려해야 함.
