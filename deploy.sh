#!/bin/bash
set -e

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}[Docker Build & Deploy Script]${NC}"

# 0. 환경 변수 확인
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env 파일이 없습니다. .env.example을 참고하여 생성하세요.${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}.env.example 파일을 .env로 복사했습니다. 내용을 수정하시기 바랍니다.${NC}"
    fi
fi

# 1. 프론트엔드 빌드 (Docker 내부에서 수행하도록 변경되었지만, 로컬 테스트를 위해 유지하거나 선택 가능)
echo -e "${GREEN}Step 1: Building Frontend...${NC}"
# cd frontend
# npm install --legacy-peer-deps && npm run build
# cd ..
# Docker 멀티스테이지 빌드를 사용하므로 위 단계는 생략 가능합니다.

# 2. 기존 컨테이너 중지 및 삭제
echo -e "${GREEN}Step 2: Stopping existing containers...${NC}"
docker-compose down

# 3. 도커 이미지 빌드 및 실행
echo -e "${GREEN}Step 3: Building and starting Docker containers...${NC}"
docker-compose up --build -d

echo -e "${BLUE}========================================= ${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "Frontend: http://localhost:3000"
echo -e "Backend: http://localhost:8080"
echo -e "${BLUE}========================================= ${NC}"
