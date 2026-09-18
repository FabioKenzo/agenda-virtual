package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;

public record EventResponse(
        Long id,
        String titulo,
        String descricao,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        String local,
        String observacoes,
        String whatsappUrl,
        String bannerUrl,
        EventStatus status,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<StudentResponse> students) {
}
