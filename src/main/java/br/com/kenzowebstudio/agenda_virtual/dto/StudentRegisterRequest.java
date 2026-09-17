package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record StudentRegisterRequest(

        @NotBlank(message = "O nome do aluno é obrigatório") String nome,

        @NotNull(message = "A data de nascimento do aluno é obrigatória") @Past(message = "A data de nascimento deve estar no passado") LocalDate dataNascimento,

        @NotBlank(message = "A turma do aluno é obrigatória") String turma) {
}