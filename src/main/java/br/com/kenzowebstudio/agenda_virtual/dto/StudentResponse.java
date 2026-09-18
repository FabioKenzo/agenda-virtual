package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String nome,
        LocalDate dataNascimento,
        String turma) {
}
