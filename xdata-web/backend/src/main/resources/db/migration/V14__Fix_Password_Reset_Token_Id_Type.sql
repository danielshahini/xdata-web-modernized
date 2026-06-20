-- PasswordResetToken.id is a Long (bigint), but xdata_password_reset_token.id was created as
-- SERIAL (int4) in V1. Align with the entity so strict Hibernate validation passes (same class of
-- drift as V13 for xdata_audit_logs).
ALTER TABLE xdata_password_reset_token ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE IF EXISTS xdata_password_reset_token_id_seq AS BIGINT;
