-- Insert ROLE_USER
INSERT INTO roles (role_name, active)
VALUES ('ROLE_USER', true)
ON CONFLICT (role_name) DO NOTHING;

-- Insert ROLE_ADMIN
INSERT INTO roles (role_name, active)
VALUES ('ROLE_ADMIN', true)
ON CONFLICT (role_name) DO NOTHING;
