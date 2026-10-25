CREATE TABLE workflow_template (
    id      TEXT    PRIMARY KEY,
    version INTEGER NOT NULL,
    body    TEXT    NOT NULL
);

CREATE TABLE workflow_template_log (
    log_id        TEXT    PRIMARY KEY,
    template_id   TEXT    NOT NULL,
    version       INTEGER NOT NULL,
    snapshot      TEXT    NOT NULL,
    registered_at TEXT    NOT NULL
);

CREATE INDEX idx_workflow_template_log_template ON workflow_template_log (template_id, version);

CREATE TABLE workflow (
    id               TEXT    PRIMARY KEY,
    template_id      TEXT    NOT NULL,
    template_version INTEGER NOT NULL,
    body             TEXT    NOT NULL
);

CREATE TABLE saga_log (
    log_id        TEXT PRIMARY KEY,
    saga_id       TEXT NOT NULL,
    snapshot      TEXT NOT NULL,
    registered_at TEXT NOT NULL
);

CREATE INDEX idx_saga_log_saga ON saga_log (saga_id, log_id);
