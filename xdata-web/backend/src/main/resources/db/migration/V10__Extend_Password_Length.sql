-- Passwörter können durch Verschlüsselung länger werden
ALTER TABLE xdata_db_connections ALTER COLUMN db_password TYPE VARCHAR(512);
