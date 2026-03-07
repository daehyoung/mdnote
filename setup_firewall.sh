#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}[Ubuntu Firewall (UFW) Configuration Script]${NC}"

# 1. UFW 설치 확인
if ! command -v ufw &> /dev/null
then
    echo -e "${YELLOW}ufw 가 설치되어 있지 않습니다. 설치를 진행합니다...${NC}"
    sudo apt-get update && sudo apt-get install -y ufw
fi

# 2. 기본 정책 설정 (모든 들어오는 요청 차단, 나가는 요청 허용)
echo -e "${GREEN}기본 보안 정책 설정 중...${NC}"
sudo ufw default deny incoming
sudo ufw default allow outgoing

# 3. 필수 포트 허용
echo -e "${GREEN}필수 서비스 포트 개방 중...${NC}"

# SSH (22번 포트 - 접속 유지 필수)
sudo ufw allow ssh

# HTTP/HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Frontend (Nginx/Docker mapped port)
sudo ufw allow 3000/tcp

# Backend API (Spring Boot/Docker mapped port)
sudo ufw allow 8080/tcp

# 4. UFW 활성화
echo -e "${YELLOW}방화벽을 활성화하시겠습니까? (y/n)${NC}"
read -r answer
if [[ "$answer" =~ ^[Yy]$ ]]; then
    sudo ufw --force enable
    echo -e "${GREEN}방화벽이 활성화되었습니다.${NC}"
else
    echo -e "${YELLOW}방화벽 활성화를 건너뜁니다. (설정만 유지)${NC}"
fi

# 5. 상태 확인
echo -e "${BLUE}========================================= ${NC}"
sudo ufw status numbered
echo -e "${BLUE}========================================= ${NC}"
echo -e "${GREEN}Firewall configuration complete!${NC}"
