-- Progressive hints for a question (one per line), revealed step by step.
ALTER TABLE xdata_queries ADD COLUMN IF NOT EXISTS hints TEXT;
