package br.com.kenzowebstudio.agenda_virtual.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/announcements")
@Tag(name = "Comunicados", description = "Enpoints para consulta e gerenciamento de comunicados escolares")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {

        this.announcementService = announcementService;
    }

    @PostMapping
    @Operation(summary = "Criar comunicado", description = "Criar um novo comunicado, apenas administradores")
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal User user) {

        AnnouncementResponse response = announcementService.create(request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    @Operation(summary = "Listar comunicados", description = "Retorna os comunicados cadastrados")
    public ResponseEntity<List<AnnouncementResponse>> findAll() {

        return ResponseEntity.ok(announcementService.findAll());

    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar comunicado por ID", description = "Retorna os detalhes de um comunicado especifico")
    public ResponseEntity<AnnouncementResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar comunicado", description = "Atualiza os dados de um comunicado existente, apenas administradores")
    public ResponseEntity<AnnouncementResponse> update(@PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir comunicado", description = "Exclui comunicado existente, apenas administradores")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        announcementService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
