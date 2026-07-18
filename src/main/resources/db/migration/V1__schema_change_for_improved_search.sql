-- ============================================================================
-- V1__schema_change_for_improved_search.sql (PostgreSQL)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1. Drop legacy tables in dependency-safe order (children before parents)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS report_tag CASCADE;
DROP TABLE IF EXISTS tag CASCADE;
DROP TABLE IF EXISTS vote CASCADE;
DROP TABLE IF EXISTS comment CASCADE;
DROP TABLE IF EXISTS solution CASCADE;
DROP TABLE IF EXISTS error_report CASCADE;
DROP TABLE IF EXISTS user_data CASCADE;

DROP TABLE IF EXISTS profession CASCADE;
DROP TABLE IF EXISTS programming_language CASCADE;
DROP TABLE IF EXISTS framework CASCADE;
DROP TABLE IF EXISTS operating_system CASCADE;

-- ---------------------------------------------------------------------------
-- 2. Dictionary Tables
-- ---------------------------------------------------------------------------
CREATE TABLE profession (
    id           UUID          NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    usage_count  INT           NOT NULL DEFAULT 0,
    is_approved  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT uq_profession_name UNIQUE (name)
);

CREATE TABLE programming_language (
    id           UUID          NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    usage_count  INT           NOT NULL DEFAULT 0,
    is_approved  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT uq_language_name UNIQUE (name)
);

CREATE TABLE framework (
    id           UUID          NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    usage_count  INT           NOT NULL DEFAULT 0,
    is_approved  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT uq_framework_name UNIQUE (name)
);

CREATE TABLE operating_system (
    id           UUID          NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    usage_count  INT           NOT NULL DEFAULT 0,
    is_approved  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT uq_os_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- 3. user_data  — rebuilt with UUID PK
-- ---------------------------------------------------------------------------
CREATE TABLE user_data (
    id                     UUID         NOT NULL,
    username               VARCHAR(20)  NOT NULL,
    email                  VARCHAR(50)  NOT NULL,
    password               VARCHAR(255) NOT NULL,
    full_name              VARCHAR(100) NOT NULL,
    bio                    VARCHAR(255) NULL,
    profession_id          UUID         NULL,
    reputation             INT          NOT NULL DEFAULT 0,
    is_account_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_account_terminated  BOOLEAN      NOT NULL DEFAULT FALSE,
    role                   SMALLINT     NULL,
    created_at             TIMESTAMP    NOT NULL,
    email_updated_at       TIMESTAMP    NOT NULL,
    username_updated_at    TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_user_data_username UNIQUE (username),
    CONSTRAINT uq_user_data_email    UNIQUE (email),
    CONSTRAINT fk_user_data_profession FOREIGN KEY (profession_id) REFERENCES profession (id)
);

-- ---------------------------------------------------------------------------
-- 4. error_report
-- ---------------------------------------------------------------------------
CREATE TABLE error_report (
    id                 UUID          NOT NULL,
    author_id          UUID          NOT NULL,
    title              VARCHAR(100)  NOT NULL,
    description        TEXT          NOT NULL,
    reproduction_steps TEXT          NULL,
    error_type         VARCHAR(50)   NOT NULL,
    repository_url     VARCHAR(200)  NULL,
    branch             VARCHAR(100)  NULL,
    commit_hash        VARCHAR(100)  NULL,
    file_path          VARCHAR(200)  NULL,
    relevant_code      TEXT          NOT NULL,
    relevant_log       TEXT          NULL,
    language_id        UUID          NOT NULL,
    language_version   VARCHAR(10)   NULL,
    framework_id       UUID          NULL,
    framework_version  VARCHAR(10)   NULL,
    os_id              UUID          NULL,
    os_version         VARCHAR(10)   NULL,
    status             SMALLINT      NOT NULL,
    score              INT           NOT NULL DEFAULT 0,
    view_count         INT           NOT NULL DEFAULT 0,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_error_report_author FOREIGN KEY (author_id) REFERENCES user_data (id),
    CONSTRAINT fk_er_language FOREIGN KEY (language_id) REFERENCES programming_language (id),
    CONSTRAINT fk_er_framework FOREIGN KEY (framework_id) REFERENCES framework (id),
    CONSTRAINT fk_er_os FOREIGN KEY (os_id) REFERENCES operating_system (id)
);

-- ---------------------------------------------------------------------------
-- 5. solution
-- ---------------------------------------------------------------------------
CREATE TABLE solution (
    id                 UUID          NOT NULL,
    error_report_id    UUID          NOT NULL,
    author_id          UUID          NOT NULL,
    probable_cause     TEXT          NULL,
    explanation        TEXT          NOT NULL,
    code_change        TEXT          NULL,
    language_id        UUID          NOT NULL,
    language_version   VARCHAR(10)   NULL,
    framework_id       UUID          NULL,
    framework_version  VARCHAR(10)   NULL,
    os_id              UUID          NULL,
    os_version         VARCHAR(10)   NULL,
    status             SMALLINT      NOT NULL,
    score              INT           NOT NULL DEFAULT 0,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_solution_error_report FOREIGN KEY (error_report_id) REFERENCES error_report (id) ON DELETE CASCADE,
    CONSTRAINT fk_solution_author FOREIGN KEY (author_id) REFERENCES user_data (id),
    CONSTRAINT fk_sol_language FOREIGN KEY (language_id) REFERENCES programming_language (id),
    CONSTRAINT fk_sol_framework FOREIGN KEY (framework_id) REFERENCES framework (id),
    CONSTRAINT fk_sol_os FOREIGN KEY (os_id) REFERENCES operating_system (id)
);

-- ---------------------------------------------------------------------------
-- 6. comment  — polymorphic (parentId + parentType)
-- ---------------------------------------------------------------------------
CREATE TABLE comment (
    id           UUID          NOT NULL,
    author_id    UUID          NOT NULL,
    parent_id    UUID          NOT NULL,
    parent_type  VARCHAR(20)   NOT NULL,
    type         SMALLINT      NOT NULL,
    body         TEXT          NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES user_data (id)
);

-- ---------------------------------------------------------------------------
-- 7. vote  — polymorphic (parentId + parentType)
-- ---------------------------------------------------------------------------
CREATE TABLE vote (
    id           UUID          NOT NULL,
    user_id      UUID          NOT NULL,
    parent_id    UUID          NOT NULL,
    parent_type  VARCHAR(20)   NOT NULL,
    vote_type    SMALLINT      NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_vote_user FOREIGN KEY (user_id) REFERENCES user_data (id),
    CONSTRAINT uq_vote_user_parent UNIQUE (user_id, parent_id, parent_type)
);

-- ---------------------------------------------------------------------------
-- 8. tag
-- ---------------------------------------------------------------------------
CREATE TABLE tag (
    id           UUID          NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    description  VARCHAR(200)  NULL,
    usage_count  INT           NOT NULL DEFAULT 0,
    is_approved  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_tag_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- 9. error_report_tag  — join table for ErrorReport <-> Tag (M:N)
-- ---------------------------------------------------------------------------
CREATE TABLE error_report_tag (
    error_report_id  UUID  NOT NULL,
    tag_id           UUID  NOT NULL,

    PRIMARY KEY (error_report_id, tag_id),
    CONSTRAINT fk_ert_error_report FOREIGN KEY (error_report_id) REFERENCES error_report (id) ON DELETE CASCADE,
    CONSTRAINT fk_ert_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);