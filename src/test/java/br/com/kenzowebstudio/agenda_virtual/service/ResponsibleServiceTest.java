package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.Event;
import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import java.time.LocalDateTime;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.model.Announcement;
import br.com.kenzowebstudio.agenda_virtual.repository.AnnouncementRepository;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleProfileResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.UpdateResponsibleProfileRequest;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import br.com.kenzowebstudio.agenda_virtual.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ResponsibleServiceTest {

        @Mock
        private StudentRepository studentRepository;

        @Mock
        private EventRepository eventRepository;

        @Mock
        private AnnouncementRepository announcementRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private ResponsibleService responsibleService;

        @Test
        void deveBuscarAlunosDoResponsavel() {

                // Arrange
                User responsavel = User.builder()
                                .id(10L)
                                .nome("Responsável Teste")
                                .build();

                Student aluno1 = Student.builder()
                                .id(1L)
                                .nome("Aluno Um")
                                .dataNascimento(LocalDate.of(2020, 5, 10))
                                .turma("Infantil I")
                                .user(responsavel)
                                .build();

                Student aluno2 = Student.builder()
                                .id(2L)
                                .nome("Aluno Dois")
                                .dataNascimento(LocalDate.of(2019, 8, 15))
                                .turma("Infantil II")
                                .user(responsavel)
                                .build();

                when(studentRepository.findByUserId(10L))
                                .thenReturn(List.of(aluno1, aluno2));

                // Act
                List<ResponsibleStudentResponse> response = responsibleService.findStudents(responsavel);

                // Assert
                assertEquals(2, response.size());

                assertEquals(1L, response.get(0).id());
                assertEquals("Aluno Um", response.get(0).nome());
                assertEquals("Infantil I", response.get(0).turma());

                assertEquals(2L, response.get(1).id());
                assertEquals("Aluno Dois", response.get(1).nome());
                assertEquals("Infantil II", response.get(1).turma());

                verify(studentRepository).findByUserId(10L);
        }

        @Test
        void deveBuscarEventosDosAlunosDoResponsavel() {

                // Arrange
                User responsavel = User.builder()
                                .id(10L)
                                .nome("Responsável Teste")
                                .build();

                Event evento1 = Event.builder()
                                .id(100L)
                                .titulo("Reunião de Pais")
                                .descricao("Reunião geral")
                                .data(LocalDate.of(2026, 10, 10))
                                .horaInicio(LocalTime.of(18, 0))
                                .horaFim(LocalTime.of(19, 30))
                                .local("Auditório")
                                .status(EventStatus.AGENDADO)
                                .build();

                Event evento2 = Event.builder()
                                .id(101L)
                                .titulo("Festa da Escola")
                                .descricao("Festa comemorativa")
                                .data(LocalDate.of(2026, 11, 20))
                                .horaInicio(LocalTime.of(14, 0))
                                .horaFim(LocalTime.of(17, 0))
                                .local("Pátio")
                                .status(EventStatus.AGENDADO)
                                .build();

                when(eventRepository
                                .findEventsVisibleToResponsible(10L))
                                .thenReturn(List.of(evento1, evento2));

                // Act
                List<ResponsibleEventResponse> response = responsibleService.findEvents(responsavel);

                // Assert
                assertEquals(2, response.size());

                assertEquals(100L, response.get(0).id());
                assertEquals("Reunião de Pais", response.get(0).titulo());
                assertEquals(LocalDate.of(2026, 10, 10), response.get(0).data());

                assertEquals(101L, response.get(1).id());
                assertEquals("Festa da Escola", response.get(1).titulo());
                assertEquals(LocalDate.of(2026, 11, 20), response.get(1).data());

                verify(eventRepository)
                                .findEventsVisibleToResponsible(10L);
        }

        @Test
        void deveBuscarSomenteComunicadosAtivos() {

                // Arrange
                LocalDateTime now = LocalDateTime.now();

                Announcement comunicado1 = Announcement.builder()
                                .id(1L)
                                .titulo("Reunião Escolar")
                                .conteudo("Reunião com os responsáveis")
                                .ativo(true)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                Announcement comunicado2 = Announcement.builder()
                                .id(2L)
                                .titulo("Feriado")
                                .conteudo("Não haverá aula")
                                .ativo(true)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                when(announcementRepository
                                .findByAtivoTrueOrderByCreatedAtDesc())
                                .thenReturn(List.of(comunicado1, comunicado2));

                // Act
                List<AnnouncementResponse> response = responsibleService.findAnnouncements();

                // Assert
                assertEquals(2, response.size());

                assertEquals(1L, response.get(0).id());
                assertEquals("Reunião Escolar", response.get(0).titulo());
                assertEquals(true, response.get(0).ativo());

                assertEquals(2L, response.get(1).id());
                assertEquals("Feriado", response.get(1).titulo());
                assertEquals(true, response.get(1).ativo());

                verify(announcementRepository)
                                .findByAtivoTrueOrderByCreatedAtDesc();
        }

        @Test
        void deveAtualizarPerfilQuandoSenhaAtualEstiverCorreta() {

                // Arrange
                LocalDateTime dataAnterior = LocalDateTime.of(2026, 1, 1, 10, 0);

                User responsavel = User.builder()
                                .id(10L)
                                .nome("Responsável Antigo")
                                .email("responsavel@teste.com")
                                .telefone("11911111111")
                                .senha("hash-bcrypt")
                                .role(Role.RESPONSAVEL)
                                .updatedAt(dataAnterior)
                                .build();

                UpdateResponsibleProfileRequest request = new UpdateResponsibleProfileRequest(
                                "Responsável Atualizado",
                                "11999999999",
                                "senhaCorreta");

                when(userRepository.findById(10L))
                                .thenReturn(Optional.of(responsavel));

                when(passwordEncoder.matches("senhaCorreta", "hash-bcrypt"))
                                .thenReturn(true);

                // Act
                ResponsibleProfileResponse response = responsibleService.updateProfile(responsavel, request);

                // Assert
                assertEquals("Responsável Atualizado", response.nome());
                assertEquals("11999999999", response.telefone());
                assertEquals("responsavel@teste.com", response.email());
                assertEquals(Role.RESPONSAVEL, response.role());

                assertEquals("Responsável Atualizado", responsavel.getNome());
                assertEquals("11999999999", responsavel.getTelefone());

                assertNotNull(responsavel.getUpdatedAt());
                assertFalse(responsavel.getUpdatedAt().isBefore(dataAnterior));

                verify(userRepository).findById(10L);
                verify(passwordEncoder).matches("senhaCorreta", "hash-bcrypt");
        }

        @Test
        void naoDeveAtualizarPerfilQuandoSenhaAtualEstiverIncorreta() {

                // Arrange
                User responsavel = User.builder()
                                .id(10L)
                                .nome("Responsável Original")
                                .email("responsavel@teste.com")
                                .telefone("11911111111")
                                .senha("hash-bcrypt")
                                .role(Role.RESPONSAVEL)
                                .build();

                UpdateResponsibleProfileRequest request = new UpdateResponsibleProfileRequest(
                                "Nome Alterado",
                                "11999999999",
                                "senhaErrada");

                when(userRepository.findById(10L))
                                .thenReturn(Optional.of(responsavel));

                when(passwordEncoder.matches("senhaErrada", "hash-bcrypt"))
                                .thenReturn(false);

                // Act
                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> responsibleService.updateProfile(responsavel, request));

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
                assertEquals("Senha atual incorreta.", exception.getReason());

                assertEquals("Responsável Original", responsavel.getNome());
                assertEquals("11911111111", responsavel.getTelefone());
                assertEquals("responsavel@teste.com", responsavel.getEmail());

                verify(userRepository).findById(10L);
                verify(passwordEncoder).matches("senhaErrada", "hash-bcrypt");
        }

        @Test
        void naoDeveAtualizarPerfilQuandoResponsavelNaoExistir() {

                // Arrange
                User responsavel = User.builder()
                                .id(10L)
                                .build();

                UpdateResponsibleProfileRequest request = new UpdateResponsibleProfileRequest(
                                "Novo Nome",
                                "11999999999",
                                "senhaAtual");

                when(userRepository.findById(10L))
                                .thenReturn(Optional.empty());

                // Act
                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> responsibleService.updateProfile(responsavel, request));

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
                assertEquals("Responsável não encontrado.", exception.getReason());

                verify(userRepository).findById(10L);
                verify(passwordEncoder, never())
                                .matches("senhaAtual", "senhaAtual");
        }

        @Test
        void devePermitirRemoverTelefoneDoPerfil() {

                // Arrange
                User responsavel = User.builder()
                                .id(10L)
                                .nome("Responsável Teste")
                                .email("responsavel@teste.com")
                                .telefone("11999999999")
                                .senha("hash-bcrypt")
                                .role(Role.RESPONSAVEL)
                                .build();

                UpdateResponsibleProfileRequest request = new UpdateResponsibleProfileRequest(
                                "Responsável Teste",
                                "   ",
                                "senhaCorreta");

                when(userRepository.findById(10L))
                                .thenReturn(Optional.of(responsavel));

                when(passwordEncoder.matches("senhaCorreta", "hash-bcrypt"))
                                .thenReturn(true);

                // Act
                ResponsibleProfileResponse response = responsibleService.updateProfile(responsavel, request);

                // Assert
                assertNull(response.telefone());
                assertNull(responsavel.getTelefone());

                assertEquals("Responsável Teste", response.nome());
                assertEquals("responsavel@teste.com", response.email());

                verify(userRepository).findById(10L);
                verify(passwordEncoder).matches("senhaCorreta", "hash-bcrypt");
        }

}