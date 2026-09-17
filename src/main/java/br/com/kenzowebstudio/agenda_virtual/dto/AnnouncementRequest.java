package br.com.kenzowebstudio.agenda_virtual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnnouncementRequest(

        @NotBlank(message = "O título é obrigatório") String titulo,

        @NotBlank(message = "O conteúdo é obrigatório") String conteudo,

        @NotNull(message = "O campo ativo é obrigatório") Boolean ativo) {
}
