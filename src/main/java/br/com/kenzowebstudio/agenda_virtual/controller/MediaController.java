package br.com.kenzowebstudio.agenda_virtual.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import br.com.kenzowebstudio.agenda_virtual.model.Media;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;

@RestController
@RequestMapping("/admin/media")
@Tag(name = "Biblioteca de mídia", description = "Gerenciamento de imagens da Agenda Virtual Escolar")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar imagem", description = "Armazena uma imagem enviada por um administrador")
    public ResponseEntity<MediaResponse> upload(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user) {

        Media media = mediaService.upload(file, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MediaResponse.fromEntity(media));
    }

    @GetMapping
    @Operation(summary = "Listar imagens", description = "Retorna as imagens cadastradas na biblioteca de mídia, ordenadas das mais recentes para as mais antigas")
    public ResponseEntity<List<MediaResponse>> listAll() {

        List<MediaResponse> images = mediaService.listAll();

        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "Visualizar imagem", description = "Retorna o conteúdo de uma imagem cadastrada na biblioteca de mídia")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {

        MediaFileResponse file = mediaService.loadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline")
                .header(
                        "X-Content-Type-Options",
                        "nosniff")
                .body(file.resource());
    }
}
