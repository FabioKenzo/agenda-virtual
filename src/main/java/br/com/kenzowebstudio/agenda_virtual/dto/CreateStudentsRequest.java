package br.com.kenzowebstudio.agenda_virtual.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStudentsRequest(

        @NotEmpty(message = "Informe pelo menos um aluno.") @Size(max = 10, message = "É permitido cadastrar no máximo 10 alunos por vez.") List<@Valid @NotNull(message = "Os dados do aluno são obrigatórios.") CreateStudentRequest> alunos

) {
}
