package br.com.kenzowebstudio.agenda_virtual.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import br.com.kenzowebstudio.agenda_virtual.controller.EventController;
import br.com.kenzowebstudio.agenda_virtual.controller.MediaController;
import br.com.kenzowebstudio.agenda_virtual.controller.ResponsibleController;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import br.com.kenzowebstudio.agenda_virtual.service.EventService;
import br.com.kenzowebstudio.agenda_virtual.service.MediaService;
import org.springframework.context.annotation.Import;
import br.com.kenzowebstudio.agenda_virtual.config.SecurityConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import br.com.kenzowebstudio.agenda_virtual.dto.EventRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.EventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaResponse;
import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.controller.AnnouncementController;
import br.com.kenzowebstudio.agenda_virtual.service.AnnouncementService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import br.com.kenzowebstudio.agenda_virtual.model.Media;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.util.List;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;
import org.springframework.core.io.ByteArrayResource;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;

@WebMvcTest(controllers = {
                ResponsibleController.class,
                EventController.class,
                AnnouncementController.class,
                MediaController.class
})

@Import(SecurityConfig.class)
class SecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ResponsibleService responsibleService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @MockitoBean
        private EventService eventService;

        @MockitoBean
        private PasswordEncoder passwordEncoder;

        @MockitoBean
        private AnnouncementService announcementService;

        @MockitoBean
        private MediaService mediaService;

        @Test
        void deveBloquearEndpointDoResponsavelSemAutenticacao() throws Exception {

                mockMvc.perform(
                                get("/responsavel/alunos"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void devePermitirEndpointDoResponsavelParaRoleResponsavel() throws Exception {

                mockMvc.perform(
                                get("/responsavel/alunos"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
        void deveBloquearEndpointDoResponsavelParaRoleAdmin() throws Exception {

                mockMvc.perform(
                                get("/responsavel/alunos"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void deveBloquearCriacaoDeEventoParaRoleResponsavel() throws Exception {

                mockMvc.perform(
                                post("/events")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
        void devePermitirCriacaoDeEventoParaRoleAdmin() throws Exception {

                EventResponse response = new EventResponse(
                                1L,
                                "Reunião de Pais",
                                "Reunião com os responsáveis",
                                LocalDate.of(2026, 10, 10),
                                LocalTime.of(18, 0),
                                LocalTime.of(19, 0),
                                "Auditório",
                                "Chegar com 10 minutos de antecedência",
                                "https://wa.me/exemplo",
                                "https://exemplo.com/banner.jpg",
                                EventStatus.AGENDADO,
                                1L,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                Set.of());

                when(eventService.create(
                                any(EventRequest.class),
                                any(User.class))).thenReturn(response);

                String json = """
                                {
                                    "titulo": "Reunião de Pais",
                                    "descricao": "Reunião com os responsáveis",
                                    "data": "2026-10-10",
                                    "horaInicio": "18:00:00",
                                    "horaFim": "19:00:00",
                                    "local": "Auditório",
                                    "observacoes": "Chegar com 10 minutos de antecedência",
                                    "whatsappUrl": "https://wa.me/exemplo",
                                    "bannerUrl": "https://exemplo.com/banner.jpg",
                                    "status": "AGENDADO",
                                    "studentIds": []
                                }
                                """;

                mockMvc.perform(
                                post("/events")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(json))
                                .andExpect(status().isCreated());
        }

        @Test
        void deveRetornar401QuandoTokenJwtForInvalido() throws Exception {

                when(jwtService.extractUsername("token-invalido"))
                                .thenThrow(new io.jsonwebtoken.MalformedJwtException(
                                                "Token JWT inválido"));

                mockMvc.perform(
                                get("/responsavel/alunos")
                                                .header("Authorization", "Bearer token-invalido"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void deveRetornar401QuandoTokenJwtEstiverExpirado() throws Exception {

                when(jwtService.extractUsername("token-expirado"))
                                .thenThrow(new io.jsonwebtoken.ExpiredJwtException(
                                                null,
                                                null,
                                                "Token JWT expirado"));

                mockMvc.perform(
                                get("/responsavel/alunos")
                                                .header("Authorization", "Bearer token-expirado"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void deveBloquearListagemAdministrativaDeEventosParaResponsavel()
                        throws Exception {

                mockMvc.perform(
                                get("/events"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void deveBloquearListagemAdministrativaDeComunicadosParaResponsavel()
                        throws Exception {

                mockMvc.perform(
                                get("/announcements"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
        void devePermitirListagemAdministrativaDeEventosParaAdmin()
                        throws Exception {

                mockMvc.perform(
                                get("/events"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
        void devePermitirListagemAdministrativaDeComunicadosParaAdmin()
                        throws Exception {

                mockMvc.perform(
                                get("/announcements"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void devePermitirListagemDeComunicadosParaResponsavel()
                        throws Exception {

                mockMvc.perform(
                                get("/responsavel/comunicados"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "responsavel@teste.com", roles = "RESPONSAVEL")
        void deveBloquearUploadDeImagemParaResponsavel() throws Exception {

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                new byte[] { 1, 2, 3 });

                mockMvc.perform(
                                multipart("/admin/media")
                                                .file(file)
                                                .with(csrf()))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(mediaService);
        }

        @Test

        void devePermitirUploadDeImagemParaAdmin() throws Exception {

                User admin = User.builder()
                                .id(1L)
                                .nome("Administrador Teste")
                                .email("admin@teste.com")
                                .senha("senha-teste")
                                .role(Role.ADMIN)
                                .ativo(true)
                                .build();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                new byte[] { 1, 2, 3 });

                Media media = Media.builder()
                                .id(10L)
                                .originalFilename("banner.png")
                                .storedFilename("imagem-teste.png")
                                .contentType("image/png")
                                .fileSize(3L)
                                .createdAt(LocalDateTime.now())
                                .build();

                when(mediaService.upload(any(), any(User.class)))
                                .thenReturn(media);

                mockMvc.perform(
                                multipart("/admin/media")
                                                .file(file)
                                                .with(user(admin))
                                                .with(csrf()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(10))
                                .andExpect(jsonPath("$.originalFilename").value("banner.png"))
                                .andExpect(jsonPath("$.contentType").value("image/png"))
                                .andExpect(jsonPath("$.fileSize").value(3));

                verify(mediaService).upload(any(), org.mockito.ArgumentMatchers.same(admin));
        }

        @Test
        void deveRetornar413QuandoUploadExcederLimite() throws Exception {

                User admin = User.builder()
                                .id(1L)
                                .nome("Administrador Teste")
                                .email("admin@teste.com")
                                .senha("senha-teste")
                                .role(Role.ADMIN)
                                .ativo(true)
                                .build();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                new byte[] { 1, 2, 3 });

                when(mediaService.upload(any(), any(User.class)))
                                .thenThrow(new MaxUploadSizeExceededException(5 * 1024 * 1024));

                mockMvc.perform(
                                multipart("/admin/media")
                                                .file(file)
                                                .with(user(admin))
                                                .with(csrf()))
                                .andExpect(status().is(413))
                                .andExpect(jsonPath("$.status").value(413))
                                .andExpect(jsonPath("$.message").value(
                                                "O arquivo enviado ultrapassa o tamanho máximo permitido."));
        }

        @Test
        void deveRetornar400QuandoImagemForInvalida() throws Exception {

                User admin = User.builder()
                                .id(1L)
                                .nome("Administrador Teste")
                                .email("admin@teste.com")
                                .senha("senha-teste")
                                .role(Role.ADMIN)
                                .ativo(true)
                                .build();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "arquivo-invalido.png",
                                "image/png",
                                new byte[] { 1, 2, 3 });

                when(mediaService.upload(any(), any(User.class)))
                                .thenThrow(new IllegalArgumentException(
                                                "O conteúdo do arquivo não corresponde a uma imagem válida."));

                mockMvc.perform(
                                multipart("/admin/media")
                                                .file(file)
                                                .with(user(admin))
                                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value(
                                                "O conteúdo do arquivo não corresponde a uma imagem válida."));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void devePermitirListagemDeImagensParaAdmin() throws Exception {

                MediaResponse image = new MediaResponse(
                                10L,
                                "banner.png",
                                "image/png",
                                1024L,
                                LocalDateTime.of(2026, 10, 30, 14, 0));

                when(mediaService.listAll())
                                .thenReturn(List.of(image));

                mockMvc.perform(get("/admin/media"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(10))
                                .andExpect(jsonPath("$[0].originalFilename").value("banner.png"))
                                .andExpect(jsonPath("$[0].contentType").value("image/png"))
                                .andExpect(jsonPath("$[0].fileSize").value(1024));

                verify(mediaService).listAll();
        }

        @Test
        @WithMockUser(roles = "RESPONSAVEL")
        void deveBloquearListagemDeImagensParaResponsavel() throws Exception {

                mockMvc.perform(get("/admin/media"))
                                .andExpect(status().isForbidden());

                verify(mediaService, never()).listAll();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void devePermitirVisualizacaoDeImagemParaAdmin() throws Exception {

                byte[] imageBytes = new byte[] { 1, 2, 3, 4 };

                MediaFileResponse file = new MediaFileResponse(
                                new ByteArrayResource(imageBytes),
                                "image/png");

                when(mediaService.loadFile(10L))
                                .thenReturn(file);

                mockMvc.perform(get("/admin/media/10/file"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("image/png"))
                                .andExpect(content().bytes(imageBytes))
                                .andExpect(header().string(
                                                "X-Content-Type-Options",
                                                "nosniff"));

                verify(mediaService).loadFile(10L);
        }

        @Test
        @WithMockUser(roles = "RESPONSAVEL")
        void deveBloquearVisualizacaoDeImagemParaResponsavel() throws Exception {

                mockMvc.perform(get("/admin/media/10/file"))
                                .andExpect(status().isForbidden());

                verify(mediaService, never()).loadFile(10L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deveRetornar404QuandoImagemNaoExistir() throws Exception {

                when(mediaService.loadFile(999L))
                                .thenThrow(new ResourceNotFoundException(
                                                "Imagem não encontrada."));

                mockMvc.perform(get("/admin/media/999/file"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value(
                                                "Imagem não encontrada."));

                verify(mediaService).loadFile(999L);
        }

}
