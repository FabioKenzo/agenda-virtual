ALTER TABLE events
ADD COLUMN banner_media_id BIGINT;

ALTER TABLE events
ADD CONSTRAINT fk_events_banner_media
FOREIGN KEY (banner_media_id)
REFERENCES media(id)
ON DELETE RESTRICT;

CREATE INDEX idx_events_banner_media_id
ON events(banner_media_id);
