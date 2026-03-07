# 🐛 [결함 보고서] Reviewer Feedback Report

## 1. 기본 정보
* **Ticket ID:** #ISSUE-REVIEW-001
* **보고 일시:** 2026-03-07 22:55
* **발견자(Reporter):** Reviewer (Antigravity)
* **모듈/컴포넌트명:** Security, FileStorage, DocumentService

## 2. 결함 및 개선 요청 상세 (Review Results)

### 2.1 보안 제약사항 검증 부족 (Security Constraints)
* **오류 현상 (Symptom):** `FileStorageService` 내 경로 탐색(Path Traversal) 방어 로직(`..` 체크)에 대한 검증 코드가 누락되었습니다.
* **설계서 일치 여부 (Design Match):** No (보안 제약사항 지시 미이행)
* **예상 결과 (Expected Result):** `../secret.txt`와 같은 파일명 입력 시 `RuntimeException`이 발생하는지 테스트 코드로 입증해야 함.

### 2.2 JWT 보안 무결성 검증 미흡
* **오류 현상 (Symptom):** `JwtTokenProviderTest`에서 토큰 변조(Signature mismatch)나 만료 상황에 대한 예외 케이스 검증이 부족합니다.
* **Reviewer 코멘트:** JWT는 인증의 핵심이므로, 변조된 토큰이 `validateToken`에서 `false`를 반환함을 입증하는 테스트 케이스 추가가 필요합니다.

### 2.3 그룹 권한(Department-based) 로직 검증 누락
* **오류 현상 (Symptom):** `DocumentService`의 `inSameDepartment` 로직을 통한 그룹 쓰기 권한 테스트가 `DocumentServiceTest`에 반영되지 않았습니다.
* **설계서 일치 여부 (Design Match):** No (유스케이스 상세 흐름 미구현)

### 2.4 성능 최적화 제안 (Optimization)
* **모듈명:** `DocumentService.processTags`
* **내용:** 현재 태그를 하나씩 `findById` 또는 `findByName`으로 조회하고 있습니다. 태그 개수가 많아질 경우 N+1 문제는 아니더라도 반복적인 DB IO가 발생할 수 있습니다.
* **권장 사항:** 추후 성능 마일스톤 시 Batch 조회(List findByNameIn) 방식으로 리팩토링할 것을 권고합니다.

## 3. Reviewer의 진단 및 조치 (Triage)
* **결함 분류:** [ Design Flaw / Coding Bug ]
* **할당 대상 (Assignee):** **Coder** (Antigravity - Self Correction)
* **Reviewer 코멘트:** "상기 보안 및 권한 로직 검증을 위한 테스트 케이스를 즉시 보강하십시오. 특히 Path Traversal 방어 테스트는 필수입니다."

## 4. 해결 계획 (Resolution Plan)
* **원인 분석 (Root Cause):** 
    1. `FileStorageService`에서 보안 체크(`..`)를 원본 파일명(`originalFileName`)이 아닌, 새로 생성된 `UUID` 기반 파일명(`fileName`)에 수행하고 있어 체크 로직이 실질적으로 무기력함.
    2. 초기 테스트 구현 시 해피 패스 위주로 작성되어 이러한 논리적 허점이 발견되지 않음.
* **수정 내역:**
    1. `FileStorageService.java`의 보안 체크 대상을 `originalFileName`으로 수정.
    2. `FileStorageServiceTest`에 Path Traversal 공격 시나리오 추가 (완료).
    3. `JwtTokenProviderTest`에 만료 및 변조 토큰 검증 추가 (완료).
    4. `DocumentServiceTest`에 동일 부서원(Group Write) 성공 시나리오 추가 (완료).
* **해결 상태:** [ Resolved ]
