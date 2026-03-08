# 마크다운 노트 시스템 (MarkDown Note System)

## 프로젝트 개요
**MarkDown Note System**은 조직/카테고리 단위의 트리 구조와 마크다운 에디터를 통해 기술 문서, 업무 일지, 블로그 형태의 지식을 중앙 관리할 수 있는 모던 웹 기반 지식 관리 시스템입니다. 문서별 세밀한 권한 제어(Public/Group Read & Write)와 라이프사이클 관리를 지원하며, Vue 3(Vite) 프론트엔드와 Spring Boot 백엔드로 구성된 안정적인 3-Tier 아키텍처를 따르고 있습니다.

## 📖 핵심 공식 문서 가이드 (Docs)
개발 및 유지보수를 위해 전체 시스템 아키텍처와 기능을 체계적으로 분석하여 작성한 아래 **7종의 문서를 우선적으로 참고**하시기 바랍니다.

1. **[요구사항 정의서 (`01_requirements.md`)](./docs/01_requirements.md)**: 사용자 권한(Role) 정의 및 시스템 전반의 핵심 기능 요구사항 명세.
2. **[기능 분석표 (`02_feature_analysis.md`)](./docs/02_feature_analysis.md)**: 백엔드 모듈 구조 및 컨트롤러-기능 맵핑 분석.
3. **[시스템 아키텍처 설계 (`03_architecture.md`)](./docs/03_architecture.md)**: 기술 스택, 인증(Security) 전략, 데이터베이스 설계 사상 도식화.
4. **[프론트엔드 화면 기술서 (`04_frontend_ui_manual.md`)](./docs/04_frontend_ui_manual.md)**: 각 화면(뷰)의 와이어프레임(객체 트리) 구조 및 버튼 요소-API 엔드포인트 연동 규격 상세 리스트.
5. **[종합 테스트 계획서 (`05_test_plan.md`)](./docs/05_test_plan.md)**: 프론트/백엔드 단위(Unit) 및 통합(E2E/Integration) 테스트 케이스 전략 가이드.
6. **[통합 배포 설계서 (`06_deployment_plan.md`)](./docs/06_deployment_plan.md)**: Nginx, Spring Boot, PostgreSQL의 Docker 컨테이너 구성(`docker-compose`) 및 실행 스크립트 모음.
7. **[요구사항 추적 매트릭스(RTM) (`07_traceability_matrix.md`)](./docs/07_traceability_matrix.md)**: 요구사항(REQ)부터 유스케이스(UC), 프론트엔드 화면(UI 요소), 백엔드 API 엔드포인트까지의 양방향 기능 추적 및 맵핑 테이블.
8. **[이슈 트래커 (`08_issue_tracker.md`)](./docs/08_issue_tracker.md)**: 관리되고 있는 결함(Bug) 및 품질 개선 사항의 통합 추적 리포트.
9. **[작업 로그 (Work Logs)](./docs/worklogs/)**: 구현 단계별 기술 의사결정 및 상세 작업 내역 아카이브.
    - [2026-03-08: API 문서화 (OpenAPI/Swagger) 도입 - 계획](./docs/worklogs/20260308_api_documentation.md) / [결과](./docs/worklogs/20260308_api_documentation_walkthrough.md)

---

## 주요 기능
- [x] 일종의 지식관리 시스템
    - 용도: 기술문서 작성, 블로그 작성, 업무일지 작성, 게시판 등으로 사용가능

- [x] 사용자관리
    - [x] 사용자 조직 - 트리구조 분류, 회사/부서/팀
    - [x] 사용자별 접근 권한
    - [x] 사용자 및 연락처 관리
    - [x] 로그인/로그아웃
    - [x] 패스워드 변경
 
- [ ] 시스템 관리자 (어드민)
    - [ ] 시스템 설정 (글로벌 Config)
    - [x] 사용자 관리
    - [ ] 로그 관리
    - [x] 사용자 조직 관리
    - [x] 카테고리 관리

- [x] 사용자 조직 관리
    - [x] 조직 트리구조 관리 CRUD
    - [x] 조직별 권한 관리
    - [x] 조직별 사용자 관리 - 사용자 조직 할당 CRUD

- [x] 카테고리 분류 관리
    - [x] 문서 종류 분류 CRUD

- [x] 문서 작성 및 관리
    - [x] 문서분류(카테고리) - 트리구조 분류
    - [x] tag - 키워드, 멀티키워드 설정가능
    - [x] markdown 편집가능 (Split Preview 지원)
    - [ ] WYSIWYG 에디터 (현재 Markdown 에디터로 구현됨)
    - [x] 파일 첨부
    - [x] 문서 댓글 관리 (Thread 댓글)
    - [x] 문서 공유 관리
    - [x] 마크다운 렌더링 보기
    - [x] 메타데이터: 소유자, 작성일, 상태 등 표기
    - [x] 코멘트 허용 여부 토글
    - [x] 세밀한 권한 설정
        - [x] 일반(PUBLIC) 읽기/쓰기
        - [x] 소속 기관별(GROUP) 읽기/쓰기
    - [x] 문서 상태(라이프사이클) 관리 - DRAFT, REVIEW, PUBLISHED 등 속성 지원

- [x] 뷰 모드 (VIEW / EDIT)
    - [x] VIEW 모드: 카테고리별 문서 목록 표시 (PUBLISHED 문서 위주 권한 필터링)
    - [x] EDIT 모드: 내 부서 및 전체 문서에서 소유/쓰기 권한 있는 문서 표시
    - [x] 문서 액션: 작성, 수정, 삭제
    - [ ] 문서 액션: 복사, 이동 (향후 고도화 필요)
    - [x] 문서 검색
        - 검색 조건: 검색어(제목/내용), 문서분류, tag, 문서 상태, 작성자 정렬 기능 등 포함

## 기술 스택
* backend
    * java, spring boot 3, jpa, postgresql, open api spec yml
* frontend
    * vuejs, vuetify, pinia, router, app-layout


## 개발 절차
* 기능별 WBS 작성
* usecase 작성
* UI/UX 설계
* API 설계
* API 구현
* UI 구현
* 테스트
    - unit test
    - integration test
    - e2e test
* 배포
    * docker build
    * setup_firewall.sh (Ubuntu UFW 설정)

---

## � 실행 및 배포 (Run & Deploy)
본 시스템은 Docker를 이용한 원클릭 빌드 및 배포를 지원합니다.

### 1) 자동 배포 스크립트 (Recommended)
루트 디렉토리의 **[deploy.sh](./deploy.sh)** 스크립트를 실행하면 프론트엔드 빌드부터 DB 구동, 백엔드 연결까지 자동으로 수행됩니다.
```bash
./deploy.sh
```

### 2) 우분투 방화벽 설정 (Firewall Setup)
우분투 서버에서 서비스를 운영할 경우 필요한 포트(80, 443, 3000, 8080)를 개방해야 합니다. **[setup_firewall.sh](./setup_firewall.sh)** 스크립트를 통해 원클릭으로 설정이 가능합니다.
```bash
sudo ./setup_firewall.sh
```

### 3) 상세 가이드
배포 아키텍처 및 환경 설정에 대한 상세 정보는 **[통합 배포 설계서 (`06_deployment_plan.md`)](./docs/06_deployment_plan.md)**를 참조하시기 바랍니다.

---

## �🛠 개발 정보 (Development Info)
* **개발 구현 Model**: Gemini 3 Pro
* **Antigravity Version**: 1.19.6
* **작성자**: 조대형 ([daehyoung.cho@gmail.com](mailto:daehyoung.cho@gmail.com))
