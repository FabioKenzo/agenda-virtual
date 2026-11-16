-- Define se o evento será visível para todos os responsáveis.
-- Eventos existentes serão considerados gerais.
ALTER TABLE events
ADD COLUMN para_todos BOOLEAN NOT NULL DEFAULT TRUE;

-- Armazena as turmas selecionadas para eventos direcionados.
CREATE TABLE event_classes (
    event_id BIGINT NOT NULL,
    turma VARCHAR(50) NOT NULL,
    CONSTRAINT pk_event_classes PRIMARY KEY (event_id, turma),
    CONSTRAINT fk_event_classes_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT chk_event_classes_turma CHECK (LENGTH(TRIM(turma)) > 0)
);

-- Facilita consultas por turma.
CREATE INDEX idx_event_classes_turma ON event_classes (turma);