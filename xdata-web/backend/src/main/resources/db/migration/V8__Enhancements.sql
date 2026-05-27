-- Dozenten-Feedback zu Abgaben
ALTER TABLE xdata_student_queries ADD COLUMN IF NOT EXISTS instructor_feedback TEXT;

-- Kurs-Ankündigungen
CREATE TABLE IF NOT EXISTS xdata_announcements (
    id SERIAL PRIMARY KEY,
    course_id INTEGER REFERENCES xdata_course(course_id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) REFERENCES xdata_users(internal_user_id)
);

-- System-Einstellungen (Key-Value)
CREATE TABLE IF NOT EXISTS xdata_system_settings (
    key VARCHAR(255) PRIMARY KEY,
    value TEXT,
    description TEXT
);
