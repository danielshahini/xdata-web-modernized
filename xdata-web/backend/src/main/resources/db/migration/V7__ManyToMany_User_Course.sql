CREATE TABLE xdata_user_courses (
    user_id VARCHAR(255) NOT NULL,
    course_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, course_id),
    CONSTRAINT fk_user_courses_user FOREIGN KEY (user_id) REFERENCES xdata_users(internal_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_courses_course FOREIGN KEY (course_id) REFERENCES xdata_course(course_id) ON DELETE CASCADE
);

-- Migrate existing data
INSERT INTO xdata_user_courses (user_id, course_id)
SELECT internal_user_id, course_id FROM xdata_users WHERE course_id IS NOT NULL;

-- Remove the old column (already dropped constraint if possible, but let's be safe)
ALTER TABLE xdata_users DROP COLUMN IF EXISTS course_id;
