-- Per-student deadline extensions (Nachteilsausgleich / individual extensions).
CREATE TABLE IF NOT EXISTS xdata_deadline_extensions (
    id SERIAL PRIMARY KEY,
    assignment_id INT NOT NULL REFERENCES xdata_assignment(assignment_id) ON DELETE CASCADE,
    student_login_id VARCHAR(255) NOT NULL,
    extended_deadline TIMESTAMP NOT NULL,
    CONSTRAINT uq_extension UNIQUE (assignment_id, student_login_id)
);
