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

CREATE TABLE IF NOT EXISTS word_state (
                                          id                BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    UNIQUE KEY uk_word_id (word_id),
    INDEX idx_next_review (next_review_at),
    CONSTRAINT fk_word_state_word FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
    CREATE TABLE IF NOT EXISTS review_log (
                                              id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              word_id     BIGINT NOT NULL,
                                              state       VARCHAR(16) NOT NULL,
    reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reviewed_at (reviewed_at),
    INDEX idx_word_id (word_id),
    CONSTRAINT fk_review_log_word FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;