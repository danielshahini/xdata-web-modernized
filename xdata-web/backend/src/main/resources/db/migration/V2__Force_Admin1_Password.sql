-- Sicherstellen, dass der Admin-Benutzer admin1 existiert und das Passwort admin1 hat
-- Passwort-Hash für 'admin1' (BCrypt): $2a$10$Y5OZrGjsUicjK3pKtVphO.UghGdrSgREeS7Z6.vRSH.S1I8yS0hS6
UPDATE xdata_users 
SET password = '$2a$10$Y5OZrGjsUicjK3pKtVphO.UghGdrSgREeS7Z6.vRSH.S1I8yS0hS6', 
    role = 'ADMIN' 
WHERE login_user_id = 'admin1';

-- Falls er noch nicht existiert (wird zwar vom Java Initializer gemacht, aber zur Sicherheit):
INSERT INTO xdata_users (internal_user_id, user_name, email, password, role, login_user_id)
SELECT 'admin-uuid-fixed', 'Administrator', 'admin@xdata.com', '$2a$10$Y5OZrGjsUicjK3pKtVphO.UghGdrSgREeS7Z6.vRSH.S1I8yS0hS6', 'ADMIN', 'admin1'
WHERE NOT EXISTS (SELECT 1 FROM xdata_users WHERE login_user_id = 'admin1');
