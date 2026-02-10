-- Create databases for each service
CREATE DATABASE IF NOT EXISTS cab_user_db;
CREATE DATABASE IF NOT EXISTS cab_cab_db;
CREATE DATABASE IF NOT EXISTS cab_ride_db;
CREATE DATABASE IF NOT EXISTS cab_billing_db;
CREATE DATABASE IF NOT EXISTS cab_notification_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON cab_user_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON cab_cab_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON cab_ride_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON cab_billing_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON cab_notification_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
