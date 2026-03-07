#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}[Docker Build & Deploy Script]${NC}"

# 1. 프론트엔드 빌드 (Dockerfile이 dist 폴더를 필요로 함)
echo -e "${GREEN}Step 1: Building Frontend...${NC}"
cd frontend
npm install && npm run build
cd ..

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
