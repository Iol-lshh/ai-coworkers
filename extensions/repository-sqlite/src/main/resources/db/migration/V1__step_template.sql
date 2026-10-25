CREATE TABLE step_template (
    id      TEXT    PRIMARY KEY,
    version INTEGER NOT NULL,
    body    TEXT    NOT NULL
);

CREATE TABLE step_template_log (
    log_id        TEXT    PRIMARY KEY,
    template_id   TEXT    NOT NULL,
    version       INTEGER NOT NULL,
    snapshot      TEXT    NOT NULL,
    registered_at TEXT    NOT NULL
);

CREATE INDEX idx_step_template_log_template ON step_template_log (template_id, version);
