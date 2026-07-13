-- ============================================================================
-- V1__schema_change_for_improved_search.sql
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1. Drop legacy tables in dependency-safe order (children before parents)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `tag_used`;
DROP TABLE IF EXISTS `votes`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `posts`;
DROP TABLE IF EXISTS `tags`;
DROP TABLE IF EXISTS `user_data`;

-- ---------------------------------------------------------------------------
-- 2. user_data  — rebuilt with UUID PK
-- ---------------------------------------------------------------------------
CREATE TABLE `user_data` (
    `id`                     BINARY(16)   NOT NULL,
    `username`               VARCHAR(20)  NOT NULL,
    `email`                  VARCHAR(50)  NOT NULL,
    `password`               VARCHAR(20)  NOT NULL,
    `reputation`             INT          NOT NULL DEFAULT 0,
    `is_account_verified`    BIT(1)       NOT NULL DEFAULT 0,
    `is_account_terminated`  BIT(1)       NOT NULL DEFAULT 0,
    `role`                   TINYINT      NULL,
    `created_at`             DATETIME(6)  NOT NULL,
    `email_updated_at`       DATETIME(6)  NOT NULL,
    `username_updated_at`    DATETIME(6)  NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `uq_user_data_username` UNIQUE (`username`),
    CONSTRAINT `uq_user_data_email`    UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 3. error_report
-- ---------------------------------------------------------------------------
CREATE TABLE `error_report` (
    `id`                 BINARY(16)    NOT NULL,
    `author_id`          BINARY(16)    NOT NULL,
    `title`              VARCHAR(100)  NOT NULL,
    `description`        TEXT          NOT NULL,
    `reproduction_steps` TEXT          NULL,
    `error_type`         VARCHAR(50)   NOT NULL,
    `repository_url`     VARCHAR(200)  NULL,
    `branch`             VARCHAR(100)  NULL,
    `commit_hash`        VARCHAR(100)  NULL,
    `file_path`          VARCHAR(200)  NULL,
    `relevant_code`      TEXT          NOT NULL,
    `relevant_log`       TEXT          NULL,
    `language`           VARCHAR(20)  NOT NULL,
    `language_version`   VARCHAR(10)   NOT NULL,
    `framework`          VARCHAR(20)  NULL,
    `framework_version`  VARCHAR(10)   NULL,
    `os`                 VARCHAR(20)  NULL,
    `os_version`         VARCHAR(10)   NULL,
    `status`             TINYINT       NOT NULL,
    `score`              INT           NOT NULL DEFAULT 0,
    `view_count`         INT           NOT NULL DEFAULT 0,
    `created_at`         DATETIME(6)   NOT NULL,
    `updated_at`         DATETIME(6)   NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_error_report_author`
        FOREIGN KEY (`author_id`) REFERENCES `user_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 4. solution
-- ---------------------------------------------------------------------------
CREATE TABLE `solution` (
    `id`                 BINARY(16)    NOT NULL,
    `error_report_id`    BINARY(16)    NOT NULL,
    `author_id`          BINARY(16)    NOT NULL,
    `probable_cause`     TEXT          NULL,
    `explanation`        TEXT          NOT NULL,
    `code_change`        TEXT          NULL,
    `language`           VARCHAR(20)   NOT NULL,
    `language_version`   VARCHAR(10)   NULL,
    `framework`          VARCHAR(20)   NULL,
    `framework_version`  VARCHAR(10)   NULL,
    `os`                 VARCHAR(20)   NULL,
    `os_version`         VARCHAR(10)   NULL,
    `status`             TINYINT       NOT NULL,
    `score`              INT           NOT NULL DEFAULT 0,
    `created_at`         DATETIME(6)   NOT NULL,
    `updated_at`         DATETIME(6)   NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_solution_error_report`
        FOREIGN KEY (`error_report_id`) REFERENCES `error_report` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_solution_author`
        FOREIGN KEY (`author_id`) REFERENCES `user_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 5. comment  — polymorphic (parentId + parentType)
-- ---------------------------------------------------------------------------
CREATE TABLE `comment` (
    `id`           BINARY(16)    NOT NULL,
    `author_id`    BINARY(16)    NOT NULL,
    `parent_id`    BINARY(16)    NOT NULL,
    `parent_type`  VARCHAR(20)   NOT NULL,
    `type`         TINYINT       NOT NULL,
    `body`         TEXT          NOT NULL,
    `created_at`   DATETIME(6)   NOT NULL,
    `updated_at`   DATETIME(6)   NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_comment_author`
        FOREIGN KEY (`author_id`) REFERENCES `user_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 6. vote  — polymorphic (parentId + parentType)
-- ---------------------------------------------------------------------------
CREATE TABLE `vote` (
    `id`           BINARY(16)    NOT NULL,
    `user_id`      BINARY(16)    NOT NULL,
    `parent_id`    BINARY(16)    NOT NULL,
    `parent_type`  VARCHAR(20)   NOT NULL,
    `vote_type`    TINYINT       NOT NULL,
    `created_at`   DATETIME(6)   NOT NULL,
    `updated_at`   DATETIME(6)   NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_vote_user`
        FOREIGN KEY (`user_id`) REFERENCES `user_data` (`id`),

    -- A user may only cast one vote per parent entity
    CONSTRAINT `uq_vote_user_parent`
        UNIQUE (`user_id`, `parent_id`, `parent_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 7. tag
-- ---------------------------------------------------------------------------
CREATE TABLE `tag` (
    `id`           BINARY(16)    NOT NULL,
    `name`         VARCHAR(30)   NOT NULL,
    `description`  VARCHAR(200)  NULL,
    `usage_count`  INT           NOT NULL DEFAULT 0,
    `created_at`   DATETIME(6)   NOT NULL,
    `updated_at`   DATETIME(6)   NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `uq_tag_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 8. error_report_tag  — join table for ErrorReport <-> Tag (M:N)
-- ---------------------------------------------------------------------------
CREATE TABLE `error_report_tag` (
    `error_report_id`  BINARY(16)  NOT NULL,
    `tag_id`           BINARY(16)  NOT NULL,

    PRIMARY KEY (`error_report_id`, `tag_id`),
    CONSTRAINT `fk_ert_error_report`
        FOREIGN KEY (`error_report_id`) REFERENCES `error_report` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_ert_tag`
        FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;