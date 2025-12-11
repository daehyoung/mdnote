
# 마크다운 노트 시스템

## 기능
* 일종의 지식관리 시스템
    용도 - 기술문서 작성, 블로그 작성, 업무일지 작성, 게시판 등으로 사용가능

* 사용자관리
    * 사용자 조직 - 트리구조 분류, 회사/부서/팀
    * 사용자별 접근 권한
    * 사용자 및 연락처 관리
    * 로그인/로그아웃
    * 패스워드 변경
 
* 시스템 관리자
    * 시스템 설정
    * 사용자 관리
    * 로그 관리
    * 사용자 조직 관리
    * 카테고리 관리


* 사용자 조직 관리
    * 조직 트리구조 관리  CRUD
    * 조직별 권한 관리
    * 조직별 사용자 관리 - 사용자 조직 할당 CRUD

*  카테고리 분류 관리
    * 분서 종류 분류 CRUD


* 문서 작성
    * 문서분류(카테고리) - 트리구조 분류
    * tag - 키워드, 멀티키워드 설정가능
    * markdown 폅집가능 
    * WYSIWYG 에디터
    * 파일 첨부
    * 문서 댓글 관리
    * 문서 공유 관리
    * 마크다운 보기
    * 소유자
    * 커멘트 여부
    * 권한 설정
        * 일반(PUBLIC) 읽기/쓰기
        * 소속 기관별 권한 읽기/쓰기
    * 문서 상태(라이프사이클) 관리 - 작성, 승인요청, 검토, 승인, 배포, 폐기


* VIEW
    - 카테고리별 문서 목록 표시
    - PUBLISHED 문서만 표시
* EDIT
    - 카테고리별 문서 목록 표시
    - 전체 문서 표시



* 문서 관리 - 문서 작성, 수정, 삭제, 복사, 이동, 복사
    * 문서 상태(라이프사이클) 관리 - 작성, 승인요청, 검토, 승인, 배포, 폐기
    * 문서 검색 
        - 소유자, 작성일, 문서명, 문서분류, tag, 문서 상태

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





curl -X 'POST' \
  'http://localhost:8080/api/v1/auth/login' \                                    
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "admin",
  "password": "admin"
}' 
 




