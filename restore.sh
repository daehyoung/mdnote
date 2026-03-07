#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

BACKUP_DIR="./backups"
DB_CONTAINER="md-note_postgres_1"
DB_NAME="mdnote"
DB_USER="postgres"

echo -e "${BLUE}[Database & Files Restore Script]${NC}"

# 1. 백업 디렉토리 확인
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${RED}Error: 백업 디렉토리($BACKUP_DIR)가 존재하지 않습니다.${NC}"
    exit 1
fi

# 2. DB 백업 파일 선택
echo -e "${YELLOW}--- Database Restore ---${NC}"
DB_BACKUPS=($(ls $BACKUP_DIR/db_${DB_NAME}_*.sql.gz 2>/dev/null))

if [ ${#DB_BACKUPS[@]} -eq 0 ]; then
    echo -e "사용 가능한 DB 백업 파일이 없습니다. (건너뜀)"
else
    echo "복구할 DB 백업 파일을 선택하세요:"
    for i in "${!DB_BACKUPS[@]}"; do
        echo "  [$i] ${DB_BACKUPS[$i]}"
    done
    echo "  [q] 무시하고 다음으로 이동"

    read -p "선택 (번호/q): " db_choice
    if [[ "$db_choice" =~ ^[0-9]+$ ]] && [ "$db_choice" -lt "${#DB_BACKUPS[@]}" ]; then
        SELECTED_DB="${DB_BACKUPS[$db_choice]}"
        echo -e "${GREEN}Selected: $SELECTED_DB${NC}"
        
        echo -e "${YELLOW}데이터베이스를 복구하면 현재 데이터가 덮어씌워집니다. 진행하시겠습니까? (y/n)${NC}"
        read -p "> " confirm
        if [[ "$confirm" =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}Restoring Database...${NC}"
            gunzip -c "$SELECTED_DB" | docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME"
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}Database restore successful.${NC}"
            else
                echo -e "${RED}Database restore failed.${NC}"
            fi
        fi
    fi
fi

# 3. 업로드 파일 백업 선택
echo -e "\n${YELLOW}--- Upload Files Restore ---${NC}"
UPLOAD_BACKUPS=($(ls $BACKUP_DIR/uploads_*.tar.gz 2>/dev/null))

if [ ${#UPLOAD_BACKUPS[@]} -eq 0 ]; then
    echo -e "사용 가능한 업로드 백업 파일이 없습니다. (건너뜀)"
else
    echo "복구할 파일 백업을 선택하세요:"
    for i in "${!UPLOAD_BACKUPS[@]}"; do
        echo "  [$i] ${UPLOAD_BACKUPS[$i]}"
    done
    echo "  [q] 종료"

    read -p "선택 (번호/q): " file_choice
    if [[ "$file_choice" =~ ^[0-9]+$ ]] && [ "$file_choice" -lt "${#UPLOAD_BACKUPS[@]}" ]; then
        SELECTED_FILES="${UPLOAD_BACKUPS[$file_choice]}"
        echo -e "${GREEN}Selected: $SELECTED_FILES${NC}"
        
        echo -e "${YELLOW}파일을 복구하면 기존 업로드 파일이 덮어씌워질 수 있습니다. 진행하시겠습니까? (y/n)${NC}"
        read -p "> " confirm
        if [[ "$confirm" =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}Restoring Upload Files...${NC}"
            tar -xzf "$SELECTED_FILES" -C .
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}Files restore successful.${NC}"
            else
                echo -e "${RED}Files restore failed.${NC}"
            fi
        fi
    fi
fi

echo -e "\n${BLUE}Restore process completed.${NC}"
