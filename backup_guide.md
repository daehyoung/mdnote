# Backup & Restore Guide for md-note

This guide explains how to use the automated backup script and how to restore data if needed.

## Backup

The `backup.sh` script automates the backup process.

### How to use
Run the script from the project root:
```bash
./backup.sh
```

### What it does
1. **Database**: Dumps the `mdnote` PostgreSQL database to `backups/db_mdnote_YYYYMMDD_HHMMSS.sql.gz`.
2. **Uploads**: Compresses the `./backend/uploads` directory to `backups/uploads_YYYYMMDD_HHMMSS.tar.gz`.
3. **Retention**: Deletes backups older than 7 days to save disk space.

---

## Restore

### 1. Restore Database
To restore the database from a backup file:
```bash
# 1. Unzip the backup file
gunzip backups/db_mdnote_YYYYMMDD_HHMMSS.sql.gz

# 2. Restore using docker exec
docker exec -i md-note_postgres_1 psql -U postgres -d mdnote < backups/db_mdnote_YYYYMMDD_HHMMSS.sql
```

### 2. Restore Upload Files
To restore uploaded files:
```bash
# 1. Extract the tarball (be careful as it might overwrite existing files)
tar -xzf backups/uploads_YYYYMMDD_HHMMSS.tar.gz -C .
```

---

## Automation (Optional)
To run this backup daily at 2 AM, add a cron job:
```bash
crontab -e
```
Add the following line (replace `/path/to/md-note` with the absolute path):
```bash
0 2 * * * cd /path/to/md-note && ./backup.sh >> ./backups/backup.log 2>&1
```
