package br.com.kenzowebstudio.agenda_virtual.dto;

import java.time.LocalDate;

public record StudentRegisterRequest(
    String nome,
    LocalDate dataNascimento,
    String turma
) {


}
