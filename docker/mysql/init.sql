USE cet_vocab;

CREATE TABLE IF NOT EXISTS word (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    text     VARCHAR(64) NOT NULL,
    phonetic VARCHAR(64),
    meanings TEXT NOT NULL,
    example  TEXT,
    level    VARCHAR(32) NOT NULL,
    UNIQUE KEY uk_word_text (text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    is_guest      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS word_state (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT NOT NULL,
    word_id           BIGINT NOT NULL,
    state             VARCHAR(16) NOT NULL,
    ease_factor       DOUBLE NOT NULL DEFAULT 2.5,
    interval_minutes  BIGINT NOT NULL DEFAULT 0,
    repetitions       INT NOT NULL DEFAULT 0,
    lapses            INT NOT NULL DEFAULT 0,
    next_review_at    DATETIME NULL,
    last_reviewed_at  DATETIME NULL,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_word (user_id, word_id),
    INDEX idx_word_id (word_id),
    INDEX idx_next_review (next_review_at),
    INDEX idx_user_next_review (user_id, next_review_at),
    CONSTRAINT fk_word_state_word FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    word_id     BIGINT NOT NULL,
    state       VARCHAR(16) NOT NULL,
    reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reviewed_at (reviewed_at),
    INDEX idx_word_id (word_id),
    INDEX idx_user_reviewed_at (user_id, reviewed_at),
    CONSTRAINT fk_review_log_word FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS word_family (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    lemma_id        BIGINT NOT NULL,
    member_text     VARCHAR(64) NOT NULL,
    relation_type   VARCHAR(8) NOT NULL,
    member_phonetic VARCHAR(64),
    member_meaning  TEXT,
    INDEX idx_lemma (lemma_id),
    CONSTRAINT fk_word_family_lemma FOREIGN KEY (lemma_id) REFERENCES word(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dict_entry (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    word        VARCHAR(64) NOT NULL,
    phonetic    VARCHAR(64),
    translation TEXT,
    definition  TEXT,
    tag         VARCHAR(128),
    exchange    VARCHAR(512),
    UNIQUE KEY uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS collected_word (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    word         VARCHAR(64) NOT NULL,
    phonetic     VARCHAR(64),
    translation  TEXT,
    context      TEXT,
    collected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_word (user_id, word),
    INDEX idx_collected_at (collected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    word_id    BIGINT NOT NULL,
    role       VARCHAR(16) NOT NULL,
    content    TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_word_id (user_id, word_id, id),
    CONSTRAINT fk_chat_word FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
