package br.com.kenzowebstudio.agenda_virtual.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/responsavel")
@Tag(name = "Responsavel", description = "Endpoints para consulta dos dados relacionados ao responsavel autenticado")
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    public ResponsibleController(ResponsibleService responsibleService) {
        this.responsibleService = responsibleService;
    }

    @GetMapping("/alunos")
    @Operation(summary = "Listar alunos do responsavel", description = "Retorna os alunos vinculados ao responsavel autenticado")
    public ResponseEntity<List<ResponsibleStudentResponse>> findStudents(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(responsibleService.findStudents(user));
    }

    @GetMapping("/eventos")
    @Operation(summary = "Listar eventos dos alunos", description = "Retorna os eventos associados aos alunos do responsavel autenticado")
    public ResponseEntity<List<ResponsibleEventResponse>> findeEvents(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(responsibleService.findEvents(user));

    }
}
