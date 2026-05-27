-- Add enabled flag to users
ALTER TABLE xdata_users ADD COLUMN enabled BOOLEAN DEFAULT TRUE;
