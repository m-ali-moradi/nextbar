-- =============================================================================
-- NextBar — MySQL Database Initialization Script
-- =============================================================================
-- Automatically creates all service databases on first MySQL container start.
-- Mounted into /docker-entrypoint-initdb.d/ via Docker Compose.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS bar_db;
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS event_db;
CREATE DATABASE IF NOT EXISTS drop_points_db;
CREATE DATABASE IF NOT EXISTS warehouse_db;
