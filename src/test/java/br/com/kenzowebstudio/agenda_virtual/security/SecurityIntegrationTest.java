package br.com.kenzowebstudio.agenda_virtual.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import br.com.kenzowebstudio.agenda_virtual.controller.EventController;
import br.com.kenzowebstudio.agenda_virtual.controller.ResponsibleController;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import br.com.kenzowebstudio.agenda_virtual.service.EventService;
import org.springframework.context.annotation.Import;
import br.com.kenzowebstudio.agenda_virtual.config.SecurityConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import br.com.kenzowebstudio.agenda_virtual.dto.EventRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.EventResponse;
import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import br.com.kenzowebstudio.agenda_virtual.model.User;

import br.com.kenzowebstudio.agenda_virtual.controller.AnnouncementController;
import br.com.kenzowebstudio.agenda_virtual.service.AnnouncementService;

@WebMvcTest(controllers = {
                ResponsibleController.class,
                EventController.class,
                AnnouncementController.class
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

}
