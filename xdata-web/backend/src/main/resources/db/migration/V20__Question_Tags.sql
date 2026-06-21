-- Topic tags for questions (e.g. "JOIN, GROUP BY"), comma-separated.
ALTER TABLE xdata_queries ADD COLUMN IF NOT EXISTS tags VARCHAR(255);
