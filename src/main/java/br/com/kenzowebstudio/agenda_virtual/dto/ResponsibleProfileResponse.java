package br.com.kenzowebstudio.agenda_virtual.dto;

import br.com.kenzowebstudio.agenda_virtual.model.Role;

public record ResponsibleProfileResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Role role) {

}
