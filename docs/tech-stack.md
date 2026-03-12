# 🛠 기술 스택 명세 (Tech Stack Specification)

## 1. 기술 스택 결정 (Core Stack)
### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **UI Library**: Vuetify 3
- **State Management**: Pinia
- **Routing**: Vue Router

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Build Tool**: Maven
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security

### Database
- **Engine**: PostgreSQL 15 (Alpine)
- **Schema Management**: Hibernate DDL Auto (Update)

---

## 2. 배포 및 실행 환경 (Infrastructure)
### Docker 
- **Frontend**: Nginx 기반 멀티스테이지 빌드 (Self-contained)
- **Backend**: JRE 17 기반 하이브리드 빌드
- **Orchestration**: Docker Compose (3.8)

### Nginx
- **Role**: Reverse Proxy & Static File Serving
- **Routing**: `/api` 경로를 백엔드(8080)로 프록시 패스

---

## 3. 보안 정책 (Security Policy)
### JWT (JSON Web Token)
- **Algorithm**: HS256
- **Storage**: Client-side (Local Storage/Cookie)
- **Authentication**: JWT Filter 기반 Stateless 인증
- **Authorization**: Role-based Access Control (ADMIN, USER)
