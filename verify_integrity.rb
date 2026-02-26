#!/usr/bin/env ruby

require 'set'

# Configuration
DB_CONTAINER = "md-note_postgres_1"
DB_NAME = "mdnote"
DB_USER = "postgres"
UPLOAD_DIR = "./backend/uploads"

puts "Fetching file records from database..."
# Get DB file names
db_files_raw = `docker exec #{DB_CONTAINER} psql -U #{DB_USER} -d #{DB_NAME} -t -c "SELECT file_name FROM attachment"`
db_files = db_files_raw.split("\n").map(&:strip).reject(&:empty?).to_set

puts "Scanning filesystem..."
# Get filesystem file names (just the filename)
fs_files = Dir.entries(UPLOAD_DIR).reject { |f| %w[. ..].include?(f) }.to_set

puts "Analyzing integrity..."
puts "------------------------------------"
puts "Total files in DB: #{db_files.size}"
puts "Total files in FS: #{fs_files.size}"

missing_in_fs = db_files - fs_files
orphans_in_fs = fs_files - db_files

if missing_in_fs.empty?
  puts "[SUCCESS] All #{db_files.size} files in DB exist in the filesystem."
else
  puts "[WARNING] #{missing_in_fs.size} files are missing in FS:"
  missing_in_fs.each { |f| puts " - #{f}" }
end

if orphans_in_fs.empty?
  puts "[INFO] No orphan files in the filesystem."
else
  puts "[INFO] #{orphans_in_fs.size} orphan files found in FS (not in DB):"
  # Limit output if there are too many
  orphans_in_fs.to_a.first(10).each { |f| puts " - #{f}" }
  puts " ... and more" if orphans_in_fs.size > 10
end
puts "------------------------------------"
