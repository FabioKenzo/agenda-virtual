package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDateTime;

public record AnnouncementResponse(
    Long id,
    String titulo, 
    String conteudo, 
    Boolean ativo, 
    Long createdBy, 
    LocalDateTime createdAt, 
    LocalDateTime updatedAt
) {

}
