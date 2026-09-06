package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;

public record EventRequest(
        String titulo,
        String descricao,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        String local,
        String observacoes,
        String whatsappUrl,
        String bannerUrl,
        EventStatus status) {

}
