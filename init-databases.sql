-- Initialize Databases for Microservices
-- This script runs automatically on the first container startup

-- 1. Payment Service
CREATE DATABASE payment_db;

-- 2. Library Service
CREATE DATABASE library_db;

-- 3. Infrastructure Service
CREATE DATABASE infrastructure_db;

-- 4. Course Service (Separated from student_db)
CREATE DATABASE course_db;

-- 5. Enrollment Service (Separated from student_db)
CREATE DATABASE enrollment_db;

-- 6. Student Service (Default DB)
-- Note: 'student_db' is created automatically by the POSTGRES_DB environment variable in docker-compose.yml.
-- However, we can include it here safely (it will fail silently if exists or be ignored).
-- To be explicit:
-- CREATE DATABASE student_db; 
