-- Optional fixed test dataset (INSERT statements) the instructor generates/imports ONCE per
-- assignment. When present, grading runs both queries against an ephemeral scratch DB seeded
-- with this data instead of regenerating per submission or relying on a live connection.
ALTER TABLE xdata_assignment ADD COLUMN IF NOT EXISTS seed_sql TEXT;
