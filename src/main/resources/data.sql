-- Seed projects
INSERT INTO projects (id, name)
VALUES ('payments', 'Payments');

INSERT INTO projects (id, name)
VALUES ('mobile', 'Mobile');

-- Seed feature flags
INSERT INTO feature_flags (name, state, project_id)
VALUES ('checkout-v2', 'ON', 'payments');

INSERT INTO feature_flags (name, state, project_id)
VALUES ('checkout-v3', 'DEFAULT', 'payments');

INSERT INTO feature_flags (name, state, project_id)
VALUES ('checkout-v2', 'OFF', 'mobile');