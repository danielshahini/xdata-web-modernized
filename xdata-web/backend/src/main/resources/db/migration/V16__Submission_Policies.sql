-- Submission policies: max attempts per question + instructor-controlled grade release.
ALTER TABLE xdata_assignment ADD COLUMN IF NOT EXISTS max_attempts INT NULL;
ALTER TABLE xdata_assignment ADD COLUMN IF NOT EXISTS grades_released BOOLEAN NOT NULL DEFAULT TRUE;
