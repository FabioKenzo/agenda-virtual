package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CreateStudentRequest(

        @NotBlank(message = "O nome do aluno é obrigatório.") @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String nome,

        @NotNull(message = "A data de nascimento é obrigatória.") @PastOrPresent(message = "A data de nascimento não pode ser futura.") LocalDate dataNascimento,

        @NotBlank(message = "A turma é obrigatória.") @Pattern(regexp = "G[1-5]", message = "A turma deve ser G1, G2, G3, G4 ou G5.") String turma

) {
}