package br.com.kenzowebstudio.agenda_virtual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateResponsibleProfileRequest(

        @NotBlank(message = "O nome é obrigatório.") @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String telefone,

        @NotBlank(message = "A senha atual é obrigatória.") String senhaAtual

) {
}
