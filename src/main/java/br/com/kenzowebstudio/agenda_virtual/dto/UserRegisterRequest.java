package br.com.kenzowebstudio.agenda_virtual.dto;

import java.util.List;

public record UserRegisterRequest(
    String nome,
    String email,
    String senha, 
    String telefone,
    List<StudentRegisterRequest> students
) {

}
