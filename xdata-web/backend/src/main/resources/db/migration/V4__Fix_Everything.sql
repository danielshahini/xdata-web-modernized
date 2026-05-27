-- Force add created_at/updated_at to main tables
ALTER TABLE xdata_users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_course ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_course ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_assignment ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_assignment ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_schemainfo ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_schemainfo ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_student_queries ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE xdata_student_queries ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Fix column types
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_assignment' AND column_name='course_id') THEN
        ALTER TABLE xdata_assignment ALTER COLUMN course_id TYPE INTEGER USING course_id::integer;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='xdata_schemainfo' AND column_name='course_id') THEN
        ALTER TABLE xdata_schemainfo ALTER COLUMN course_id TYPE INTEGER USING course_id::integer;
    END IF;
END $$;
