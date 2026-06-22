-- Teacher-controlled difficulty per question (EASY | MEDIUM | HARD). Nullable;
-- when null the frontend falls back to deriving difficulty from the question's marks.
ALTER TABLE xdata_queries ADD COLUMN IF NOT EXISTS difficulty VARCHAR(16);
