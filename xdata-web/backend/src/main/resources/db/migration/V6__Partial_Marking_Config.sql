-- Add partial marking configuration column to xdata_queries
ALTER TABLE xdata_queries ADD COLUMN partial_mark_info TEXT;

-- Update student queries to store detailed mark info
ALTER TABLE xdata_student_queries ADD COLUMN mark_info_json TEXT;
