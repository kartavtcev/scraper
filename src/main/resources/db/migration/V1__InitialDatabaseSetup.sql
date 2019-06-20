CREATE TABLE IF NOT EXISTS headlines (
  link VARCHAR PRIMARY KEY,
  title VARCHAR NOT NULL
);
-- TODO: ORDER BY link or title for pagination