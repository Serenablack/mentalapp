CREATE TABLE suggested_activities
(
    id                         BIGINT AUTO_INCREMENT NOT NULL,
    mood_entry_id              BIGINT                NOT NULL,
    activity_description       TEXT                  NOT NULL,
    is_completed               BIT(1)                NOT NULL,
    completed_at               datetime              NULL,
    activity_type              VARCHAR(100)          NULL,
    estimated_duration_minutes INT                   NULL,
    difficulty_level           INT                   NULL,
    priority_level             INT                   NULL,
    created_at                 datetime              NOT NULL,
    updated_at                 datetime              NOT NULL,
    CONSTRAINT pk_suggested_activities PRIMARY KEY (id)
);

ALTER TABLE suggested_activities
    ADD CONSTRAINT FK_SUGGESTED_ACTIVITIES_ON_MOOD_ENTRY FOREIGN KEY (mood_entry_id) REFERENCES mood_entries (id);