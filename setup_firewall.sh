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
echo -e "${GREEN}포트 분류별 개방 정책 적용 중...${NC}"

# [Category A] 필수 서비스 포트 (Standard Web)
# 외부 브라우저 접속을 위한 표준 포트
echo -e "${BLUE}1. 서비스 포트 허용: 80, 443${NC}"
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# [Category B] 애플리케이션 런타임 포트 (App Specific)
# 도커 컨테이너와 직접 통신하거나 내부망 디버깅 시 필요
echo -e "${BLUE}2. 애플리케이션 포트 허용: 3000(FE), 8080(BE)${NC}"
sudo ufw allow 3000/tcp
sudo ufw allow 8080/tcp

# [Category C] 관리 및 디버깅 포트 (Management/Debug)
# SSH는 접속 유지를 위해 항상 허용
echo -e "${BLUE}3. 관리 포트 허용: 22(SSH)${NC}"
sudo ufw allow ssh

# PostgreSQL (데이터베이스 직접 접근 필요 시에만 주석 해제하여 사용 권장)
# sudo ufw allow 5432/tcp

# [보안 팁] 최종 서비스 시에는 Nginx 등을 통해 80/443만 외부로 노출하고
# 3000, 8080 포트는 로컬(127.0.0.1)에서만 접근하도록 설정하는 것이 안전합니다.

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
