CREATE KEYSPACE IF NOT EXISTS tpo
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
  AND durable_writes = true;

-- Crear tabla user_logs en keyspace tpo
CREATE TABLE IF NOT EXISTS tpo.user_logs (
  user_id text,
  log_date date,
  login_time timestamp,
  logout_time timestamp,
  PRIMARY KEY ((user_id, log_date), login_time)
);
