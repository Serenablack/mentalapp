CREATE TABLE mood_entries
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    user_id             BIGINT                NOT NULL,
    entry_date          datetime              NOT NULL,
    location            VARCHAR(255)          NULL,
    comfort_environment VARCHAR(50)           NULL,
    `description`       TEXT                  NULL,
    energy_level        INT                   NULL,
    passion             VARCHAR(100)          NULL,
    created_at          datetime              NOT NULL,
    updated_at          datetime              NOT NULL,
    CONSTRAINT pk_mood_entries PRIMARY KEY (id)
);

CREATE TABLE mood_entry_emotions
(
    emotion_id    BIGINT NOT NULL,
    mood_entry_id BIGINT NOT NULL,
    CONSTRAINT pk_mood_entry_emotions PRIMARY KEY (emotion_id, mood_entry_id)
);

ALTER TABLE mood_entries
    ADD CONSTRAINT FK_MOOD_ENTRIES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE mood_entry_emotions
    ADD CONSTRAINT fk_mooentemo_on_emotion FOREIGN KEY (emotion_id) REFERENCES emotions (id);

ALTER TABLE mood_entry_emotions
    ADD CONSTRAINT fk_mooentemo_on_mood_entry FOREIGN KEY (mood_entry_id) REFERENCES mood_entries (id);