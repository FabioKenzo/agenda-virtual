package br.com.kenzowebstudio.agenda_virtual.dto;

import br.com.kenzowebstudio.agenda_virtual.model.Role;

public record AuthMeResponse(
    Long id, 
    String nome, 
    String email, 
    Role role
) {

}
