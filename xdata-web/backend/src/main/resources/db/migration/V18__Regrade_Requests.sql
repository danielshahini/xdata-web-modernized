-- Regrade requests (student objections) + manual grade override audit.
CREATE TABLE IF NOT EXISTS xdata_regrade_requests (
    id SERIAL PRIMARY KEY,
    submission_id INT NOT NULL REFERENCES xdata_student_queries(submission_id) ON DELETE CASCADE,
    student_login_id VARCHAR(255) NOT NULL,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    instructor_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Track manual overrides on submissions.
ALTER TABLE xdata_student_queries ADD COLUMN IF NOT EXISTS manually_graded BOOLEAN NOT NULL DEFAULT FALSE;
