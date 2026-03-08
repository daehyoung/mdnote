# MarkDown Note System Use Cases (Value-Centric)

## 1. 개요 (Philosophy)

본 시스템의 유스케이스는 단순히 기능을 나열하는 것이 아니라, **"사용자가 시스템을 통해 달성하고자 하는 궁극적인 가치(Goal)"**를 중심으로 정의됩니다.

*   **Goal-Oriented Use Case**: 사용자가 업무를 완결 짓는 큰 단위의 흐름입니다.
*   **Interruptible Point (Traceability ID)**: 기술적 구현의 추적성과 디버깅을 위해 유지하는 '중단 가능 지점'입니다. 로그인(`UC-U-01`) 등은 독립적인 유스케이스라기보다 목적 달성을 위한 필수적인 중단점이자 추적용 식별자입니다.

---

## 2. [Goal 1] 지식 탐색 및 소비 (Knowledge Exploration & Consumption)

*   **액터**: 독자 (Reader)
*   **최종 가치**: 조직 내 축적된 지식을 빠르고 정확하게 찾아내어 업무 지능으로 활용함.
*   **사전 조건**: 시스템에 접근 가능한 유효한 계정 보유.

### 📍 실행 및 추적 포인트 (Interruptible Points)

| 단계 | 추적 ID | 중단점 (Interruptible Point) | 연동 API |
| :--- | :--- | :--- | :--- |
| **진입** | `UC-U-01` | **자격 증명 확인 및 세션 획득** | `POST /api/v1/auth/login` |
| **탐색** | `UC-D-01` | **카테고리/목록 데이터 수신** | `GET /api/v1/documents` |
| **검색** | `UC-D-04` | **전역 키워드 검색 결과 수신** | `GET /api/v1/documents?search=...` |
| **소비** | `UC-D-03` | **문서 상세 로드 및 렌더링 완료** | `GET /api/v1/documents/{id}` |
| **피드백** | `UC-CM-01`| **의견(댓글) 등록 및 저장** | `POST /api/v1/comments` |

*   **기본 흐름 (Scenario)**:
    1. 사용자가 시스템에 접속하여 인증 절차(`UC-U-01`)를 마친다.
    2. 홈 화면 또는 검색 기능을 통해 필요한 지식을 식별(`UC-D-01`, `UC-D-04`)한다.
    3. 문서의 상세 내용(마크다운, 다이어그램)을 읽고 필요한 정보를 습득(`UC-D-03`)한다.
    4. 필요 시 동료들과 해당 주제에 대해 댓글로 소통(`UC-CM-01`)한다.

---

## 3. [Goal 2] 지식 생산 및 자산화 (Knowledge Production & Assetization)

*   **액터**: 작성자 (Writer/Author)
*   **최종 가치**: 개인의 노하우나 업무 성과를 표준화된 문서로 변환하여 조직의 영구적인 자산으로 남김.
*   **사전 조건**: 문서 작성 권한이 부여된 사용자.

### 📍 실행 및 추적 포인트 (Interruptible Points)

| 단계 | 추적 ID | 중단점 (Interruptible Point) | 연동 API |
| :--- | :--- | :--- | :--- |
| **기안** | `UC-D-02` | **신규 문서 엔티티 생성 및 ID 발급** | `POST /api/v1/documents` |
| **보강** | `UC-D-05` | **미디어 파일 업로드 및 URL 확정** | `POST /api/attachments/upload` |
| **확정** | `UC-D-02-E`| **본문 및 메타데이터 최종 저장** | `PUT /api/v1/documents/{id}` |

*   **기본 흐름 (Scenario)**:
    1. 작성자가 새로운 지식 기록을 시작(`UC-D-02`)한다.
    2. 마크다운 에디터를 통해 내용을 타건하고 보조 자료(이미지 등)를 첨부(`UC-D-05`)한다.
    3. 문서의 공개 범위(Public/Group)와 라이프사이클(Draft/Published)을 설정한다.
    4. 최종적으로 "저장"을 눌러 시스템 자산으로 영구히 기록한다.

---

## 4. [Goal 3] 시스템 운영 및 보안 거버넌스 (System Governance)

*   **액터**: 관리자 (Admin)
*   **최종 가치**: 신뢰할 수 있는 사용자 환경을 조성하고, 조직의 지식 분류 체계를 체계적으로 관리하여 운영 효율성을 높임.
*   **사전 조건**: ADMIN 역할(Role) 보유.

### 📍 실행 및 추적 포인트 (Interruptible Points)

| 단계 | 추적 ID | 중단점 (Interruptible Point) | 연동 API |
| :--- | :--- | :--- | :--- |
| **계정 관리** | `UC-A-01` | **사용자 생성 / 상태 변경 / 비번 초기화** | `POST/PUT /api/admin/users/*` |
| **분류 체계** | `UC-A-03` | **공용 카테고리 트리 노드 확정** | `POST/PUT /api/categories/*` |
| **모니터링** | `UC-A-05` | **시스템 리소스 및 지식 현황 집계 수신** | *(Dashboard API)* |

*   **기본 흐름 (Scenario)**:
    1. 관리자가 조직의 구성원 변동을 반영하여 계정을 관리(`UC-A-01`)한다.
    2. 전사적인 지식 분류를 위해 시스템 공통 카테고리를 설계 및 관리(`UC-A-03`)한다.
    3. 전체 시스템의 사용 지표를 모니터링(`UC-A-05`)하여 원활한 서비스 운영을 보장한다.

---

## 5. 기타 보조 기능 (Supporting Actions)

### 5.1 개인 설정 관리 (`UC-U-02`)
*   **액터**: 모든 사용자
*   **중단점**: `PUT /api/v1/profile`
*   **가치**: 본인의 개인화된 환경(비밀번호, 테마 등)을 유지함.
