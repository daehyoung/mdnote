#!/usr/bin/env ruby

require 'set'
require 'fileutils'

# Configuration
DB_CONTAINER = "md-note_postgres_1"
DB_NAME = "mdnote"
DB_USER = "postgres"
UPLOAD_DIR = "./backend/uploads"

puts "Fetching file records from database..."
db_files_raw = `docker exec #{DB_CONTAINER} psql -U #{DB_USER} -d #{DB_NAME} -t -c "SELECT file_name FROM attachment"`
db_files = db_files_raw.split("\n").map(&:strip).reject(&:empty?).to_set

puts "Scanning filesystem..."
fs_files = Dir.entries(UPLOAD_DIR).reject { |f| %w[. ..].include?(f) }.to_set

orphans_in_fs = fs_files - db_files

if orphans_in_fs.empty?
  puts "[INFO] No orphan files to delete."
  exit
end

puts "[INFO] Found #{orphans_in_fs.size} orphan files."
puts "Deleting files..."

orphans_in_fs.each do |filename|
  file_path = File.join(UPLOAD_DIR, filename)
  if File.exist?(file_path)
    # FileUtils.rm(file_path) # Direct deletion
    # Adding a safety check to ensure we only delete files inside the upload dir
    full_path = File.expand_path(file_path)
    if full_path.start_with?(File.expand_path(UPLOAD_DIR))
       FileUtils.rm(full_path)
       print "."
    end
  end
end

puts "\n[SUCCESS] Cleanup completed. #{orphans_in_fs.size} files deleted."
