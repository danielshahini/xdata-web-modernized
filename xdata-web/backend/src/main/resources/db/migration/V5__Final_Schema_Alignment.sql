-- Fix column types for all tables referencing Course
DO $$ 
DECLARE
    constraint_to_drop TEXT;
BEGIN
    -- xdata_assignment
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_assignment' AND column_name='course_id') THEN
        -- Find and drop foreign key if exists
        SELECT constraint_name INTO constraint_to_drop
        FROM information_schema.key_column_usage 
        WHERE table_name='xdata_assignment' AND column_name='course_id' AND constraint_name LIKE 'fk%';
        
        IF constraint_to_drop IS NOT NULL THEN
            EXECUTE 'ALTER TABLE xdata_assignment DROP CONSTRAINT ' || constraint_to_drop;
        END IF;
        
        -- Use more robust conversion
        ALTER TABLE xdata_assignment ALTER COLUMN course_id TYPE INTEGER USING (CASE WHEN course_id::text ~ '^[0-9]+$' THEN course_id::integer ELSE NULL END);
    END IF;

    -- xdata_schemainfo
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_schemainfo' AND column_name='course_id') THEN
        constraint_to_drop := NULL;
        SELECT constraint_name INTO constraint_to_drop
        FROM information_schema.key_column_usage 
        WHERE table_name='xdata_schemainfo' AND column_name='course_id' AND constraint_name LIKE 'fk%';
        
        IF constraint_to_drop IS NOT NULL THEN
            EXECUTE 'ALTER TABLE xdata_schemainfo DROP CONSTRAINT ' || constraint_to_drop;
        END IF;
        
        ALTER TABLE xdata_schemainfo ALTER COLUMN course_id TYPE INTEGER USING (CASE WHEN course_id::text ~ '^[0-9]+$' THEN course_id::integer ELSE NULL END);
    END IF;

    -- xdata_users
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_users' AND column_name='course_id') THEN
        constraint_to_drop := NULL;
        SELECT constraint_name INTO constraint_to_drop
        FROM information_schema.key_column_usage 
        WHERE table_name='xdata_users' AND column_name='course_id' AND constraint_name LIKE 'fk%';
        
        IF constraint_to_drop IS NOT NULL THEN
            EXECUTE 'ALTER TABLE xdata_users DROP CONSTRAINT ' || constraint_to_drop;
        END IF;
        
        ALTER TABLE xdata_users ALTER COLUMN course_id TYPE INTEGER USING (CASE WHEN course_id::text ~ '^[0-9]+$' THEN course_id::integer ELSE NULL END);
    END IF;

    -- xdata_lms_credentials
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_lms_credentials' AND column_name='course_id') THEN
        ALTER TABLE xdata_lms_credentials ALTER COLUMN course_id TYPE INTEGER USING (CASE WHEN course_id::text ~ '^[0-9]+$' THEN course_id::integer ELSE NULL END);
    END IF;
END $$;

-- Add constraints back correctly
ALTER TABLE xdata_assignment ADD CONSTRAINT fk_assignment_course FOREIGN KEY (course_id) REFERENCES xdata_course(course_id);
ALTER TABLE xdata_schemainfo ADD CONSTRAINT fk_schema_course FOREIGN KEY (course_id) REFERENCES xdata_course(course_id);
ALTER TABLE xdata_users ADD CONSTRAINT fk_user_course FOREIGN KEY (course_id) REFERENCES xdata_course(course_id);

-- Ensure all Audit columns have no nulls
UPDATE xdata_users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE xdata_users SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE xdata_course SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE xdata_course SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE xdata_assignment SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE xdata_assignment SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE xdata_schemainfo SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE xdata_schemainfo SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
