-- Natural-language task statement per question (the actual problem text students read).
-- Previously students only saw the question's short name/title. Nullable.
ALTER TABLE xdata_queries ADD COLUMN IF NOT EXISTS description TEXT;
