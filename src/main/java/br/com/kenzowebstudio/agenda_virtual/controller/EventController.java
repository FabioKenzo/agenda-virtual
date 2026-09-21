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

import br.com.kenzowebstudio.agenda_virtual.dto.EventRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.EventResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
@Tag(name = "Eventos", description = "Endpoints para consulta e gereciamento de eventos escolares")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Criar evento", description = "Cria um novo evento, apenas administradores")
    public ResponseEntity<EventResponse> create(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {

        EventResponse response = eventService.create(request, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar Eventos", description = "Retorna os eventos cadastrados ordenados por data")
    public ResponseEntity<List<EventResponse>> findAll() {

        return ResponseEntity.ok(eventService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar eventos por ID", description = "Retorna os detalhes de um aviso especifico")
    public ResponseEntity<EventResponse> findById(@PathVariable Long id) {

        return ResponseEntity.ok(eventService.findById(id));

    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento", description = "Exclui um evento existente, apenas administradores")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento", description = "Exclui um evento existente, apenas administradores")
    public ResponseEntity<Void> delete(@PathVariable Long id){

        eventService.delete(id);
        return ResponseEntity.noContent().build();

    }
}
