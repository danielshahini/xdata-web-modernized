-- Course learning materials (links / text notes shown to students).
CREATE TABLE IF NOT EXISTS xdata_course_materials (
    id SERIAL PRIMARY KEY,
    course_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'LINK',
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
