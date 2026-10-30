package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDateTime;
import br.com.kenzowebstudio.agenda_virtual.model.Media;

public record MediaResponse(
        Long id,
        String originalFilename,
        String contentType,
        Long fileSize,
        LocalDateTime createdAt
) {

    public static MediaResponse fromEntity(Media media) {

        return new MediaResponse(
                media.getId(),
                media.getOriginalFilename(),
                media.getContentType(),
                media.getFileSize(),
                media.getCreatedAt()
        );
    }
}
