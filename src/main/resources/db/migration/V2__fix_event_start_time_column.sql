-- corrigindo erro de digitacao da coluna de horario inicial do evento
ALTER TABLE events
RENAME COLUMN hora_incio TO hora_inicio;

-- alinha o tamanho do email com o mapeamento da Entity User
ALTER TABLE users
ALTER COLUMN email TYPE VARCHAR(150);