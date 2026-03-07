# 04 프론트엔드 화면 기술서 및 매뉴얼

## 1. 개요 (Overview)
본 문서는 MarkDown Note System 프로젝트의 프론트엔드(Vue 3 + Vuetify) 구현체를 기반으로 작성된 상세 화면 기술서 및 사용자 매뉴얼입니다. 개발된 Vue 컴포넌트(`src/views/*`)와 라우팅 구조(`src/router/index.js`)를 1:1로 매핑하여 시스템의 실제 사용자 인터페이스 동작을 정의합니다. 각 화면별 **와이어프레임(Wireframe)**과 화면 요소별 **ID**를 기입하여 요소와 기능 설명을 연결하였습니다.

## 2. 공통 레이아웃 (Layout & Navigation)
모든 주요 기능 화면은 `MainLayout.vue`를 공유합니다.

### 🎨 와이어프레임 & 화면 컴포넌트 트리 (Main Layout)
```text
+-------------------------------------------------------------+
| {AppBar}                                                            |
| [≡ `#btn-menu`] [MD Note `#logo-home`]        [👀/✏️ `#toggle-mode`] |
|                             [🛡️ `#btn-admin`] [👤 `#btn-profile`] [🚪 `#btn-logout`] |
+-------------------------------------------------------------+
| {Navigation Drawer}         |        {User Screens}         |
|                             |                               |
| System Categories           |                               |
| 📁 `#list-sys-cats`          |                               |
|                             |        ================       |
| My Documents   [⚙️ `#btn-my`] |        ROUTER VIEW          |
| 📁 `#list-user-cats`         |          CONTENT            |
|                             |        ================       |
| Tags                        |                               |
| 🏷️ `#chip-tags`              |                               |
+-------------------------------------------------------------+
```

```text
[Layout] MainLayout
├── [Header] AppBar
│   ├── [Button] Toggle Menu (#btn-menu) : 좌측 내비게이션 서랍 오픈/클로즈
│   ├── [Title] App Logo (#logo-home) : 로고 및 홈 화면 전환
│   ├── [Toggle] View/Edit Mode (#toggle-mode) : 전역 읽기/쓰기 모드 스위칭
│   ├── [Button] Admin Dashboard (#btn-admin) : 관리자 화면 진입
│   ├── [Button] User Profile (#btn-profile) : 내 정보 화면 진입
│   └── [Button] Logout (#btn-logout) : 세션 종료 및 로그인 뷰 이동
└── [Container] Body Layout
    ├── [Aside] Navigation Drawer
    │   ├── [List] System Categories (#list-sys-cats) : 시스템 공통 트리 목록
    │   ├── [List] My Documents (#list-user-cats) : 개인/부서별 트리 목록
    │   │   └── [Button] Setting (#btn-my) : 개인 폴더 관리창 진입
    │   └── [Group] Tags (#chip-tags) : 태그 기반 문서 필터링 칩
    └── [Main] Router View
        └── (각 하위 User Screen 컴포넌트 렌더링 영역)
```

### 2.1 상단 앱 바 (App Bar)
화면 최상단에 고정된 내비게이션 바입니다.
* **토글 메뉴(Hamburger Menu)** (`#btn-menu`): 좌측 내비게이션 서랍(Drawer)을 열고 닫습니다.
* **타이틀** (`#logo-home`): 홈 화면으로 이동하는 로고/앱 이름 역할.
* **모드 전환 토글 (View / Edit)** (`#toggle-mode`): 앱의 전반적인 모드를 전환합니다. 
  - `VIEW`: 문서 읽기 전용 모드. 오직 'PUBLISHED(승인됨/발행됨)' 상태의 문서 위주로 조회됩니다.
  - `EDIT`: 자신이 작성하거나 편집 권한을 부여받은 모든 상태(DRAFT 등)의 문서를 조회 및 편집하는 모드입니다.
* **Admin 버튼** (`#btn-admin`): 관리자 권한 사용자 전용. 방패 아이콘 클릭 시 `/admin` 대시보드로 이동.
* **프로필 버튼** (`#btn-profile`): 본인 계정 정보(암호 등) 수정(`/profile`)으로 이동.
* **로그아웃 버튼** (`#btn-logout`): 세션을 종료하고 로그인 창(`/login`)으로 전환.

### 2.2 좌측 내비게이션 서랍 (Navigation Drawer)
문서 분류 및 필터링을 위한 고정 메뉴 영역입니다.
* **System Categories** (`#list-sys-cats`): 관리자가 설정한 전사적/공통 문서 트리 카테고리.
* **My Documents** (`#list-user-cats`): 사용자 본인 및 소속 부서용 개인 폴더 트리. 우측 톱니바퀴 버튼(`#btn-my`)으로 `/my-categories` (폴더 설정 화면) 진입 가능.
* **Tags (태그 그룹)** (`#chip-tags`): 문서에 등록되어 있는 태그 목록, 클릭 시 해당 태그 조건으로 리스트 필터링.

---

## 3. 사용자 화면 매뉴얼 (User Screens)

### 3.1 로그인 화면 (`LoginView.vue` - `/login`)
시스템 접근을 위한 최초 관문입니다.
* **핵심 요소**: Username 필드 (`#input-username`), Password 필드 (`#input-password`), "Login" 버튼 (`#btn-login`).
* **동작**: 유효한 자격 증명 후 JWT 획득. 성공하면 `/` (Home)로 라우팅. 에러 시 Alert 노출.

#### 🔌 API 연동 규격표 (LoginView)
| Req ID | Use Case ID | 기능 / 요소 ID | HTTP Method | API Endpoint | Request / Payload | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-U-01** | `UC-U-01` | **로그인 요청** <br> `#btn-login` | `POST` | `/api/v1/auth/login` | `{ "username": "...", "password": "..." }` | `200 OK` <br> `{ "token": "jwt..." }` | 자격 증명 후 발급된 JWT 토큰을 LocalStorage 등에 저장하고 전역 상태(Pinia) 갱신 |

### 3.2 홈 대시보드 화면 (`HomeView.vue` - `/`)
모든 사용자의 기본 진입점이며, 문서 목록을 조회하는 메인 화면입니다.

#### 🎨 와이어프레임 & 컴포넌트 트리 (Home Dashboard)
```text
+-------------------------------------------------------------+
| Documents                                                   |
| [🔍 Search... `#input-search`] [🔎 `#btn-search`]             |
| [Statuses ▾ `#sel-status`] [Sort By ▾ `#sel-sort`] [⇅ `#btn-sort-dir`] |
|                                          [+ New Document `#btn-new`] |
|-------------------------------------------------------------|
| 📄 `#list-item-doc`                                          |
| **Document Title**  by Author (Dept)  [STATUS]              |
| 📅 Date  🏷️ Category  [tag1] [tag2]    [🌍 Read] [👥 Read]    |
|-------------------------------------------------------------|
| ...                                                         |
+-------------------------------------------------------------+
|                 < 1 2 [3] 4 5 > `#pagination`                |
+-------------------------------------------------------------+
```

```text
[Container] HomeView
├── [Section] Toolbar
│   ├── [Input] Search Bar (#input-search) : 전역 키워드 검색
│   ├── [Button] Search Submit (#btn-search) : 검색 실행
│   ├── [Select] Status Filter (#sel-status) : 문서 상태별 필터 (EDIT 모드 전용)
│   ├── [Select] Sort Options (#sel-sort) : 정렬 기준 (제목/작성일/작성자)
│   ├── [Button] Sort Direction (#btn-sort-dir) : 오름차순/내림차순 토글
│   └── [Button] New Document (#btn-new) : 새 문서 생성 (EDIT 모드 전용)
├── [List] Document Layout
│   └── [Item] Document Card (#list-item-doc) : 단일 문서 정보 (클릭 시 상세 이동)
│       ├── [Text] Title, Author, Department, Status
│       ├── [Text] Date, Category
│       ├── [Group] Tag Chips
│       └── [Group] Permission Icons (Read/Write 상태 표시)
└── [Footer] Pagination (#pagination) : 페이지 이동
```

* **검색 및 필터 영역 (상단)**:
  - **검색 바** (`#input-search`, `#btn-search`): 제목 및 내용 기반 Full-text 검색 (Enter 타이핑 동작).
  - **상태 필터** (`#sel-status`): `EDIT` 모드일 때만 활용 가능하며 특정 주기 값(DRAFT/REVIEW 등)만 필터링.
  - **정렬 옵션** (`#sel-sort`, `#btn-sort-dir`): 제목, 작성자, 작성일 기준 정렬 및 화살표 아이콘으로 오름차/내림차순 토글.
  - **새 문서 버튼** (`#btn-new`): 누르면 백엔드에 즉시 빈 문서를 생성하고 에디터 화면으로 이동.
* **문서 리스트 영역**:
  - **리스트 아이템** (`#list-item-doc`): 개별 문서를 뜻하며, 누르면 상세 뷰어 전환. 작성자 정보와 퍼미션 상태(색상별 아이콘)가 즉시 표출됨.
* **페이징 컴포넌트** (`#pagination`): 하단에 위치, 현재 페이지 스코프 표시 및 클릭 시 서버 재요청 수행.

#### 🔌 API 연동 규격표 (Home Dashboard)
| Req ID | Use Case ID | 기능 / 요소 ID | HTTP Method | API Endpoint | Request / Payload | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-D-02** | `UC-D-01`<br>`UC-D-04` | **전체 문서 목록 조회** <br> (화면 진입 / `#pagination` / 필터 변경) | `GET` | `/api/v1/documents` | **Query Params:** <br> `page={n}`, `size={n}`, <br> `search={kw}`, `status={stat}`, <br> `sort={field,dir}`, `categoryId={id}`, `tag={name}` | `200 OK` <br> (Page 객체: `content` 배열, `totalPages` 등) | 사용자의 View/Edit 권한과 쿼리 파라미터 조합에 맞춰 필터링된 문서 페이징 데이터 수신 |
| **REQ-D-01** | `UC-D-02` | **새 문서 생성** <br> `#btn-new` | `POST` | `/api/v1/documents` | `{ "title": "Untitled", "content": "", "categoryId": ... }` | `201 Created` <br> 생성된 Document 엔티티 반환 | 빈 문서를 즉시 DRAFT 상태 등으로 생성한 뒤 응답받은 ID로 에디터 뷰(`/documents/{id}/edit`) 전환 |

### 3.3 문서 상세 보기 화면 (`DocumentDetailView.vue` - `/documents/{id}`)
문서 읽기(View 모드) 및 타 사용자들과의 댓글(Thread) 소통이 이루어지는 화면입니다.

#### 🎨 와이어프레임 & 🌳 컴포넌트 트리 (Document Detail)
```text
+-------------------------------------------------------------+
| [← `#btn-back`] Document Title                               |
| [tag] [tag] | Author (Dept) | [🌍 Read] [👥 Read] | Date    |
|                          [⬇️ `#btn-download`] [✏️ Edit `#btn-edit`] |
+-------------------------------------------------------------+
|                                                             |
|  `#markdown-body`                                            |
|  # Heading 1                                                |
|  This is the document content rendered in markdown...       |
|                                                             |
+-------------------------------------------------------------+
| Comments                                                    |
| 👤 User 1 - Date                    [🗑️ `#btn-del-comment`]  |
| This is a comment body text.                                |
|                                                             |
| [Write a comment... `#input-comment`] [Post `#btn-post-comment`]      |
+-------------------------------------------------------------+
```

```text
[Container] DocumentDetailView
├── [Header] Toolbar
│   ├── [Button] Back (#btn-back) : 목록으로 복귀
│   ├── [Text] Document Title
│   ├── [Group] Metadata : Tags, Author, Department, Permissions, Date
│   ├── [Button] Download Markdown (#btn-download) : .md 파일 다운로드
│   └── [Button] Edit (#btn-edit) : 수정 화면으로 진입 (권한 보유자 전용)
├── [Section] Content Body (#markdown-body) : 마크다운 및 Mermaid 렌더링 뷰
└── [Section] Comments (Allow Comments 활성화 시)
    ├── [List] Comment Items
    │   └── [Button] Delete (#btn-del-comment) : 본인/관리자 전용 휴지통
    ├── [Input] Write Comment (#input-comment) : 새 댓글 텍스트 입력
    └── [Button] Post (#btn-post-comment) : 댓글 서버 등록
```


* **상단 툴바**:
  - **뒤로가기** (`#btn-back`): 조회 리스트로 Return.
  - **마크다운 다운로드** (`#btn-download`): 조회 중인 내용을 로컬 PC에 `.md` 텍스트 파일로 저장 기능 동작.
  - **수정 진입** (`#btn-edit`): 사용자에게 Write 권한이 인가되었을 경우에만 툴바에 노출되는 수정 화면 진입 버튼.
* **콘텐츠 영역 (본문)**:
  - **마크다운 렌더링 컨테이너** (`#markdown-body`): 백엔드에서 내려준 원문을 Vue 마크다운 변환기와 Mermaid 다이어그램 렌더러가 번역하여 HTML 블록으로 표출.
* **댓글 영역 (Comments)**:
  - 문서의 Allow Comments가 활성화된 경우만 렌더. 새로운 댓글 내용을 타건하는 입력폼(`#input-comment`) 및 전송버튼(`#btn-post-comment`).
  - 본인 소유의 댓글일 경우 우측 상단 십자 휴지통 버튼(`#btn-del-comment`)으로 삭제 지원 가능.

#### 🔌 API 연동 규격표 (Document Detail)
| Req ID | Use Case ID | 기능 / 요소 ID | HTTP Method | API Endpoint | Request / Payload | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-D-02** | `UC-D-03` | **문서 상세 조회** <br> (화면 진입 시점) | `GET` | `/api/v1/documents/{id}` | Path Variable: `id` | `200 OK` <br> `Document` 객체 | 마크다운 본문 및 메타데이터, 작성자 정보를 포함해 응답 |
| **REQ-CM-01**| `UC-D-03` | **댓글 목록 조회** | `GET` | `/api/v1/comments?documentId={id}` | Query Params 필요 | `200 OK` <br> 댓글 배열 | 해당 문서에 달린 댓글 리스트 조회 (향후 엔드포인트 구체화 필요) |
| **REQ-CM-01**| `UC-D-03` | **댓글 작성** <br> `#btn-post-comment` | `POST` | `/api/v1/comments` | `{ "documentId": {id}, "content": "..." }` | `201 Created` | 입력한 내용을 댓글 엔티티로 생성 |
| **REQ-CM-01**| `UC-D-03` | **댓글 삭제** <br> `#btn-del-comment` | `DELETE` | `/api/v1/comments/{commentId}` | Path Variable: `commentId` | `204 No Content` | 본인이 작성한 댓글 영구 삭제 |

### 3.4 문서 에디터 화면 (`DocumentEditView.vue` - `/documents/{id}/edit`)
핵심 콘텐츠 기안/작성 동작 스크린입니다.

#### 🎨 와이어프레임 & 컴포넌트 트리 (Document Edit)
```text
+-------------------------------------------------------------+
| [← `#btn-back`] [Title Input `#input-title`]                  |
|          [Save `#btn-save`] [🗑️ `#btn-del-doc`] [📎 `#btn-attach`] [Split `#btn-view-mode`]|
+-------------------------------------------------------------+
| [Category ▾ `#sel-cat`] [Tags ▾ `#input-tags`] [Status ▾ `#sel-status`] |
|                                   [Info > `#btn-toggle-settings`]    |
+-------------------------------+-----------------------------+
| `#textarea-editor`             | `#markdown-preview`          |
| # Title                       | # Title                     |
|                               |                             |
| Typing markdown here...       | Rendered preview here...    |
+-------------------------------+-----------------------------+
```
*(※ 우측 설정 서랍(`#drawer-settings`)은 `#btn-toggle-settings` 클릭 시 우측에서 슬라이드 인(Slide-in) 형태로 일시 노출됩니다.)*

```text
[Container] DocumentEditView
├── [Header] Main Toolbar
│   ├── [Button] Back (#btn-back) : 복귀 및 Dirty Check 
│   ├── [Input] Document Title (#input-title) : 제목 입력칸
│   ├── [Button] Save (#btn-save) : 변경사항 서버 반영
│   ├── [Button] Delete (#btn-del-doc) : 영구 삭제 시도
│   ├── [Button] Upload Attachment (#btn-attach) : 파일 선택/업로드 (클립 아이콘)
│   └── [Button] Toggle View Mode (#btn-view-mode) : Split / Preview / Edit 모드 순환
├── [Header] Sub Metadata Bar
│   ├── [Select] Category (#sel-cat) : 폴더 구조 매핑
│   ├── [Combobox] Tags (#input-tags) : 다중 태그 입력
│   ├── [Select] Status (#sel-status) : 라이프사이클 지정
│   └── [Button] Settings Toggle (#btn-toggle-settings) : 우측 서랍 메뉴 열기 (Info)
├── [Section] Editor Container
│   ├── [Textarea] Markdown Editor (#textarea-editor) : 퓨어 텍스트 입력칸 (이미지 업로드 스니펫 자동 삽입)
│   └── [Div] Preview (#markdown-preview) : 렌더링 결과 실시간 확인 화면
└── [Aside] Settings Drawer (#drawer-settings) : 메타바 우측 슬라이딩 패널
    ├── [Panel] Attachments : 첨부 관리 (목록 확인, 파일 크기 확인, 개별 다운로드/삭제)
    ├── [Panel] Permissions : 그룹/Public 읽기 및 쓰기 권한 부여 체크박스들
    └── [Panel] Options : 댓글 기능 활성화/비활성화 스위치 토글
```

* **상단 편집 툴바**:
  - **제목 편집 필드** (`#input-title`).
  - **저장 버튼** (`#btn-save`): 폼 Dirty 상태 체크 후 변경점을 백엔드 Update/Put 라우팅 연동.
  - **휴지통(삭제) 버튼** (`#btn-del-doc`): 삭제 확인창 출력 후 영구 폐기.
  - **뷰 모드 토글바** (`#btn-view-mode`): 누를 때마다 Split View -> Preview Only -> Edit Only 순환.
* **문서 메타데이터 서브바**:
  - **Category 선택기** (`#sel-cat`) 및 **Tag 콤보박스** (`#input-tags`).
  - **Lifecycle 상태관리 Dropdown** (`#sel-status`).
  - **설정 서랍 여닫기** (`#btn-toggle-settings`).

#### 📎 파일 첨부 및 관리 (Attachments) 시스템
* **업로드 트리거** (`#btn-attach`): 툴바의 클립 아이콘을 누르면 OS의 파일 선택창 오픈. 비동기 Multipart 파일 전송.
* **마크다운 스니펫 주입**: 업로드가 반환되면 해당 URL을 기반으로 `#textarea-editor` 영역 내 커서 문단에 `![파일명](/api/attachments/ID)` 규격의 마크다운 텍스트 자동 삽입 처리됨.
* **첨부 리스트 관리**: `#btn-toggle-settings`으로 노출되는 사이드 서랍 패널에서 개별 파일에 대해 다운로드/삭제 관리.

#### ⚙️ 우측 설정 서랍 메뉴 (`#drawer-settings`) 추가 요소
* **Permissions (보안 등급 권한 설정표)**:
  - `Group (Department)` 체크박스: 소속 부서원 Read/Write 허용.
  - `General Public` 체크박스: 전사 Read/Write 허용 (전역 옵션).
* **Settings**: 본문의 댓글 허용 여부 토글 스위치(Allow Comments).

#### 🔌 API 연동 규격표 (Document Edit)
| Req ID | Use Case ID | 기능 / 요소 ID | HTTP Method | API Endpoint | Request / Payload | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-D-01** | `UC-D-02` | **문서 데이터 로드** | `GET` | `/api/v1/documents/{id}` | Path: `id` | `200 OK` | 기존 문서 정보를 에디터 상태에 바인딩 |
| **REQ-D-01** | `UC-D-02` | **문서 저장 / 갱신** <br> `#btn-save` | `PUT` | `/api/v1/documents/{id}` | `{ "title": "...", "content": "...", "status": "DRAFT", "categoryId": ..., "tags": [...] }` | `200 OK` <br> `Document` 갱신 반환 | 입력된 내용과 메타데이터, 권한 설정을 DB에 업데이트 |
| **REQ-D-01** | `UC-D-02` | **문서 삭제** <br> `#btn-del-doc` | `DELETE` | `/api/v1/documents/{id}` | Path: `id` | `204 No Content` | 문서를 영구 삭제하고 홈/목록 화면으로 라우팅 |
| **REQ-D-04** | `UC-D-02` | **파일 업로드** <br> `#btn-attach` | `POST` | `/api/v1/attachments/upload` | `multipart/form-data` <br> (파일 바이너리 전송) | `201 Created`/`200 OK` <br> 메타데이터 (ID, URL) | 파일을 저장하고 발급된 식별자로 마크다운 이미지 스니펫 자동 생성 |
| **REQ-D-04** | `UC-D-02` | **첨부파일 삭제** | `DELETE` | `/api/v1/attachments/{attachId}` | Path: `attachId` | `204 No Content` | 설정 서랍에서 지정된 파일의 메타데이터 및 물리 파일 폐기 |

---

## 4. 관리자 화면 매뉴얼 (Admin Console)
Admin 권한 보유자만 접근 가능한 특권 뷰 리스트(`/admin/*`).

### 4.1 시스템 사용자 관리 (`UserManageView.vue`)
* 조직 구성원 리스트를 표(Table)로 모니터링. 역할(Role)이나 비밀번호(Password)를 관리자가 강제 초기화하거나 신규 사용자를 추가합니다.

### 4.2 조직도(부서) 관리 (`OrganizationView.vue`)
* 재귀적 트리 컴포넌트를 사용하여 `Company -> Engineering -> Backend` 와 같은 조직 계층도(Department node)를 트리 형태로 관리합니다.

### 4.3 시스템 카테고리 관리 (`CategoryManageView.vue`, Scope: SYSTEM)
* 사용자의 공통 네비게이션 서랍(`#list-sys-cats`)에 보이는 `System Categories` 트리를 구축/수정합니다.

## 5. 결론
와이어프레임과 HTML ID 단위의 설계는 사용자 편의성과 개발 유지보수의 용이성을 보장합니다. 본 문서는 구현된 컴포넌트 이벤트 액션과 정확히 일치하며 마일스톤 확장의 기반 지표가 됩니다.
