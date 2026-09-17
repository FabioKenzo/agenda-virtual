package br.com.kenzowebstudio.agenda_virtual.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank(message = "O nome é obrigatório") String nome,

        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") String email,

        @NotBlank(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String senha,

        String telefone,

        @NotEmpty(message = "É necessário informar pelo menos um aluno") @Valid List<StudentRegisterRequest> students) {
}