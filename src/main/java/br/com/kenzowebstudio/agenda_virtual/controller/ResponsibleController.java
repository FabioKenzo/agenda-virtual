package br.com.kenzowebstudio.agenda_virtual.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;

@RestController
@RequestMapping("/responsavel")
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    public ResponsibleController(ResponsibleService responsibleService) {
        this.responsibleService = responsibleService;
    }

    @GetMapping("/alunos")
    public ResponseEntity<List<ResponsibleStudentResponse>> findStudents(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(responsibleService.findStudents(user));
    }
}
