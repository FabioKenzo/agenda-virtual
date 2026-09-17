package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequest(

                @NotBlank(message = "O título é obrigatório") @Size(max = 150, message = "O título deve ter no máximo 150 caracteres") String titulo,

                @NotBlank(message = "A descrição é obrigatória") String descricao,

                @NotNull(message = "A data é obrigatória") LocalDate data,

                LocalTime horaInicio,

                LocalTime horaFim,

                @Size(max = 150, message = "O local deve ter no máximo 150 caracteres") String local,

                String observacoes,

                @Size(max = 500, message = "A URL do WhatsApp deve ter no máximo 500 caracteres") String whatsappUrl,

                @Size(max = 500, message = "A URL do banner deve ter no máximo 500 caracteres") String bannerUrl,

                @NotNull(message = "O status é obrigatório") EventStatus status

) {
}
