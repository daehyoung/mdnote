#!/bin/bash

# Configuration
PROJECT_NAME="md-note"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RETENTION_DAYS=7

# PostgreSQl Configuration (from docker-compose.yml)
DB_CONTAINER="md-note_postgres_1"
DB_NAME="mdnote"
DB_USER="postgres"

# Upload Files Configuration
UPLOAD_DIR="./backend/uploads"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "Starting backup for $PROJECT_NAME at $TIMESTAMP..."

# 1. Database Backup
DB_BACKUP_FILE="$BACKUP_DIR/db_${DB_NAME}_$TIMESTAMP.sql"
echo "Backing up database $DB_NAME to $DB_BACKUP_FILE..."
docker exec $DB_CONTAINER pg_dump -U $DB_USER $DB_NAME > "$DB_BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Database backup successful."
    # Compress the SQL file
    gzip "$DB_BACKUP_FILE"
    echo "Database backup compressed: ${DB_BACKUP_FILE}.gz"
else
    echo "Database backup failed!"
    exit 1
fi

# 2. Upload Files Backup
FILES_BACKUP_FILE="$BACKUP_DIR/uploads_$TIMESTAMP.tar.gz"
echo "Backing up upload files from $UPLOAD_DIR to $FILES_BACKUP_FILE..."

if [ -d "$UPLOAD_DIR" ]; then
    tar -czf "$FILES_BACKUP_FILE" "$UPLOAD_DIR"
    if [ $? -eq 0 ]; then
        echo "Files backup successful."
    else
        echo "Files backup failed!"
    fi
else
    echo "Upload directory $UPLOAD_DIR not found, skipping files backup."
fi

# 3. Cleanup old backups
echo "Cleaning up backups older than $RETENTION_DAYS days..."
find "$BACKUP_DIR" -type f -mtime +$RETENTION_DAYS -name "*.gz" -delete

echo "Backup process completed."
