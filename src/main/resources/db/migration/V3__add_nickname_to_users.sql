ALTER TABLE users
    ADD COLUMN nickname VARCHAR(50) NOT NULL;

CREATE UNIQUE INDEX ux_users_nickname_active
    ON users (nickname)
    WHERE deleted_at IS NULL;
