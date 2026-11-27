package br.com.kenzowebstudio.agenda_virtual.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleProfileResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import br.com.kenzowebstudio.agenda_virtual.dto.UpdateResponsibleProfileRequest;
import org.springframework.web.bind.annotation.PostMapping;
import br.com.kenzowebstudio.agenda_virtual.dto.CreateStudentsRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UpdateStudentRequest;

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

        @PostMapping("/alunos")
        @Operation(summary = "Cadastrar alunos do responsável", description = "Cadastra um ou mais alunos vinculados ao responsável autenticado")
        public ResponseEntity<List<ResponsibleStudentResponse>> createStudents(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody CreateStudentsRequest request) {

                List<ResponsibleStudentResponse> alunos = responsibleService.createStudents(user, request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(alunos);
        }

        @PatchMapping("/alunos/{id}")
        @Operation(summary = "Atualizar aluno do responsável", description = "Atualiza os dados de um aluno vinculado ao responsável autenticado")
        public ResponseEntity<ResponsibleStudentResponse> updateStudent(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody UpdateStudentRequest request) {

                return ResponseEntity.ok(
                                responsibleService.updateStudent(id, user, request));
        }

        @GetMapping("/eventos")
        @Operation(summary = "Listar eventos dos alunos", description = "Retorna os eventos associados aos alunos do responsavel autenticado")
        public ResponseEntity<List<ResponsibleEventResponse>> findeEvents(
                        @AuthenticationPrincipal User user) {

                return ResponseEntity.ok(responsibleService.findEvents(user));

        }

        @GetMapping("/perfil")
        @Operation(summary = "Consultar perfil do responsável", description = "Retorna os dados do responsável autenticado")
        public ResponseEntity<ResponsibleProfileResponse> findProfile(
                        @AuthenticationPrincipal User user) {

                return ResponseEntity.ok(
                                responsibleService.findProfile(user));
        }

        @PatchMapping("/perfil")
        @Operation(summary = "Atualizar perfil do responsável", description = "Atualiza nome e telefone do responsável autenticado mediante confirmação da senha atual")
        public ResponseEntity<ResponsibleProfileResponse> updateProfile(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody UpdateResponsibleProfileRequest request) {

                return ResponseEntity.ok(
                                responsibleService.updateProfile(user, request));
        }

        @GetMapping("/eventos/paginados")
        @Operation(summary = "Listar eventos paginados dos alunos", description = "Retorna os eventos associados aos alunos do responsável autenticado com paginação")
        public ResponseEntity<Page<ResponsibleEventResponse>> findEventsPaginated(
                        @AuthenticationPrincipal User user,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "9") int size) {

                if (page < 0 || size < 1 || size > 50) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Parâmetros de paginação inválidos.");
                }

                Pageable pageable = PageRequest.of(page, size, Sort.unsorted());

                return ResponseEntity.ok(
                                responsibleService.findEventsPaginated(user, pageable));
        }

        @GetMapping("/eventos/{id}")
        @Operation(summary = "Buscar evento por ID", description = "Retorna os detalhes de um evento associado aos alunos do responsável autenticado")
        public ResponseEntity<ResponsibleEventResponse> findEventById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User user) {

                return ResponseEntity.ok(
                                responsibleService.findEventById(id, user));
        }

        @GetMapping("/eventos/{id}/banner")
        @Operation(summary = "Visualizar banner do evento", description = "Retorna o banner de um evento associado aos alunos do responsável autenticado")
        public ResponseEntity<Resource> findEventBanner(
                        @PathVariable Long id,
                        @AuthenticationPrincipal User user) {

                MediaFileResponse file = responsibleService.findEventBanner(id, user);

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(file.contentType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                                .header("X-Content-Type-Options", "nosniff")
                                .body(file.resource());
        }

        @GetMapping("/comunicados")
        @Operation(summary = "Listar comunicados ativos", description = "Retorna os comunicados ativos disponíveis para o responsavel autenticado")
        public ResponseEntity<List<AnnouncementResponse>> findAnnouncements() {

                return ResponseEntity.ok(
                                responsibleService.findAnnouncements());
        }

        @GetMapping("/comunicados/paginados")
        @Operation(summary = "Listar comunicados ativos paginados", description = "Retorna os comunicados ativos com paginação, ordenados do mais recente para o mais antigo")
        public ResponseEntity<Page<AnnouncementResponse>> findAnnouncementsPaginated(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "9") int size) {

                if (page < 0 || size < 1 || size > 50) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Parâmetros de paginação inválidos.");
                }

                Pageable pageable = PageRequest.of(page, size, Sort.unsorted());

                return ResponseEntity.ok(
                                responsibleService.findAnnouncementsPaginated(pageable));
        }

        @GetMapping("/comunicados/{id}")
        @Operation(summary = "Buscar comunicado por ID", description = "Retorna os detalhes de um comunicado ativo para o responsável autenticado")
        public ResponseEntity<AnnouncementResponse> findAnnouncementById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                responsibleService.findAnnouncementById(id));
        }

}
