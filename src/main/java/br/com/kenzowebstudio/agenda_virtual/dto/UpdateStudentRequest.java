package br.com.kenzowebstudio.agenda_virtual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateStudentRequest(

                @NotBlank @Size(max = 100) String nome,

                @NotNull @PastOrPresent LocalDate dataNascimento,

                @NotBlank @Pattern(regexp = "G[1-5]") String turma,

                @NotBlank String senhaAtual
) {
}
