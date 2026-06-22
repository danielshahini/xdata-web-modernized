-- AuditLog.id is a Long (bigint), but xdata_audit_logs.id was created as SERIAL (int4) in V1.
-- Strict Hibernate schema validation (ddl-auto=validate) requires the column type to match the
-- entity. `update` silently tolerated the mismatch; `validate` correctly rejects it.
ALTER TABLE xdata_audit_logs ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE IF EXISTS xdata_audit_logs_id_seq AS BIGINT;
