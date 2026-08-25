CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY, 
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'RESPONSAVEL',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK(role IN('RESPONSAVEL', 'ADMIN'))
);

CREATE TABLE students(
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    turma VARCHAR(50), 
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_students_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE events(
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT NOT NULL,
    data DATE NOT NULL,
    hora_incio TIME,
    hora_fim TIME,
    local VARCHAR(150),
    observacoes TEXT,
    whatsapp_url VARCHAR(500),
    banner_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_events_status
        CHECK (status IN ('AGENDADO', 'CANCELADO', 'FINALIZADO')),

    CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)

);

CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    conteudo TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_announcements_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE TABLE event_students (
    event_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,

    PRIMARY KEY (event_id, student_id),

    CONSTRAINT fk_event_students_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_students_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
        ON DELETE CASCADE
);