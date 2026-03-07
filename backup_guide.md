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

### 1. Automated Restore (Recommended)
You can use the `restore.sh` script to interactively choose a backup to restore:
```bash
chmod +x restore.sh
./restore.sh
```

### 2. Manual Restore (Advanced)
If you prefer to do it manually:

#### Database Restore
```bash
# Unzip and restore in one pipe
gunzip -c backups/db_mdnote_YYYYMMDD_HHMMSS.sql.gz | docker exec -i md-note_postgres_1 psql -U postgres -d mdnote
```

#### Upload Files Restore
```bash
# Extract the tarball
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
