-- Brute-force lockout + forced password change (self-service password policy).
ALTER TABLE xdata_users ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE xdata_users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP NULL;
ALTER TABLE xdata_users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
