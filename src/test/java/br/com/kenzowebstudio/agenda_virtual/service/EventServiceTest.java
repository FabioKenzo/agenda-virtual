package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.kenzowebstudio.agenda_virtual.dto.EventRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.EventResponse;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.model.Event;
import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import java.util.Optional;
import br.com.kenzowebstudio.agenda_virtual.repository.MediaRepository;
import br.com.kenzowebstudio.agenda_virtual.model.Media;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

        @Mock
        private EventRepository eventRepository;

        @Mock
        private StudentRepository studentRepository;

        @InjectMocks
        private EventService eventService;

        @Mock
        private MediaRepository mediaRepository;

        @Test
        void deveCriarEventoComSucesso() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Student student = Student.builder()
                                .id(1L)
                                .nome("Aluno Teste")
                                .dataNascimento(LocalDate.of(2020, 5, 10))
                                .turma("Infantil II")
                                .build();

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento comemorativo da escola",
                                LocalDate.of(2026, 10, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                "Levar garrafa de água",
                                "https://wa.me/exemplo",
                                "https://exemplo.com/banner.jpg",
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(1L), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(studentRepository.findAllById(Set.of(1L)))
                                .thenReturn(List.of(student));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> {
                                        Event event = invocation.getArgument(0);
                                        event.setId(100L);
                                        return event;
                                });

                // Act
                EventResponse response = eventService.create(request, admin);

                // Assert
                assertEquals(100L, response.id());
                assertEquals("Festa da Escola", response.titulo());
                assertEquals(EventStatus.AGENDADO, response.status());
                assertEquals(10L, response.createdBy());
                assertEquals(1, response.students().size());

                ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

                verify(eventRepository).save(eventCaptor.capture());

                Event capturedEvent = eventCaptor.getValue();

                assertEquals("Festa da Escola", capturedEvent.getTitulo());
                assertEquals(admin, capturedEvent.getCreatedBy());
                assertEquals(1, capturedEvent.getStudents().size());

                assertEquals(true, response.paraTodos());
                assertEquals(Set.of(), response.turmas());
                assertEquals(true, capturedEvent.getParaTodos());
                assertEquals(Set.of(), capturedEvent.getTurmas());
        }

        @Test
        void deveCriarEventoParaTodosMesmoComTurmasInformadas() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento para todos os responsáveis",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of("G1", "G2") // turmas
                );

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> {
                                        Event event = invocation.getArgument(0);
                                        event.setId(100L);
                                        return event;
                                });

                // Act
                EventResponse response = eventService.create(request, admin);

                // Assert
                assertEquals(100L, response.id());
                assertEquals(true, response.paraTodos());
                assertEquals(Set.of(), response.turmas());

                ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

                verify(eventRepository).save(eventCaptor.capture());

                Event capturedEvent = eventCaptor.getValue();

                assertEquals(true, capturedEvent.getParaTodos());
                assertEquals(Set.of(), capturedEvent.getTurmas());

                verify(studentRepository, never()).findDistinctTurmas();
        }

        @Test
        void deveCriarEventoParaTurmasEspecificasComSucesso() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião G1 e G2",
                                "Reunião exclusiva para responsáveis do G1 e G2",
                                LocalDate.of(2026, 11, 25),
                                LocalTime.of(14, 0),
                                LocalTime.of(16, 0),
                                "Sala de reuniões",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                false, // paraTodos
                                Set.of("G1", "G2") // turmas
                );

                when(studentRepository.findDistinctTurmas())
                                .thenReturn(Set.of("G1", "G2", "G3", "G4", "G5"));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> {
                                        Event event = invocation.getArgument(0);
                                        event.setId(101L);
                                        return event;
                                });

                // Act
                EventResponse response = eventService.create(request, admin);

                // Assert
                assertEquals(101L, response.id());
                assertEquals(false, response.paraTodos());
                assertEquals(Set.of("G1", "G2"), response.turmas());

                ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

                verify(eventRepository).save(eventCaptor.capture());

                Event capturedEvent = eventCaptor.getValue();

                assertEquals(false, capturedEvent.getParaTodos());
                assertEquals(Set.of("G1", "G2"), capturedEvent.getTurmas());

                verify(studentRepository).findDistinctTurmas();
        }

        @Test
        void deveLancarExcecaoAoCriarEventoSemTurmasEspecificas() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião de Responsáveis",
                                "Evento exclusivo para turmas selecionadas",
                                LocalDate.of(2026, 11, 25),
                                LocalTime.of(14, 0),
                                LocalTime.of(16, 0),
                                "Sala de reuniões",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                false, // paraTodos
                                Set.of() // nenhuma turma selecionada
                );

                // Act & Assert
                assertThrows(
                                IllegalArgumentException.class,
                                () -> eventService.create(request, admin));

                verify(eventRepository, never()).save(any(Event.class));
                verify(studentRepository, never()).findDistinctTurmas();
        }

        @Test
        void deveLancarExcecaoAoCriarEventoComTurmaInexistente() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião G9",
                                "Evento para uma turma inexistente",
                                LocalDate.of(2026, 11, 25),
                                LocalTime.of(14, 0),
                                LocalTime.of(16, 0),
                                "Sala de reuniões",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                false, // paraTodos
                                Set.of("G9") // turma inexistente
                );

                when(studentRepository.findDistinctTurmas())
                                .thenReturn(Set.of("G1", "G2", "G3", "G4", "G5"));

                // Act & Assert
                assertThrows(
                                IllegalArgumentException.class,
                                () -> eventService.create(request, admin));

                verify(studentRepository).findDistinctTurmas();

                verify(eventRepository, never())
                                .save(any(Event.class));
        }

        @Test
        void deveAtualizarEventoDeTodosParaTurmasEspecificas() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Event eventoExistente = Event.builder()
                                .id(100L)
                                .titulo("Evento para Todos")
                                .descricao("Evento aberto a todos")
                                .data(LocalDate.of(2026, 11, 20))
                                .horaInicio(LocalTime.of(14, 0))
                                .horaFim(LocalTime.of(16, 0))
                                .local("Pátio da escola")
                                .status(EventStatus.AGENDADO)
                                .createdBy(admin)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .students(Set.of())
                                .paraTodos(true)
                                .turmas(Set.of())
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião G1 e G2",
                                "Evento exclusivo para G1 e G2",
                                LocalDate.of(2026, 11, 25),
                                LocalTime.of(14, 0),
                                LocalTime.of(16, 0),
                                "Sala de reuniões",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                false, // paraTodos
                                Set.of("G1", "G2") // turmas
                );

                when(studentRepository.findDistinctTurmas())
                                .thenReturn(Set.of("G1", "G2", "G3", "G4", "G5"));

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(eventoExistente));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                EventResponse response = eventService.update(100L, request);

                // Assert
                assertEquals(100L, response.id());
                assertEquals(false, response.paraTodos());
                assertEquals(Set.of("G1", "G2"), response.turmas());

                assertEquals(false, eventoExistente.getParaTodos());
                assertEquals(Set.of("G1", "G2"), eventoExistente.getTurmas());

                verify(studentRepository).findDistinctTurmas();
                verify(eventRepository).save(eventoExistente);
        }

        @Test
        void deveAtualizarEventoDeTurmasEspecificasParaTodos() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Event eventoExistente = Event.builder()
                                .id(100L)
                                .titulo("Reunião G1 e G2")
                                .descricao("Evento exclusivo para G1 e G2")
                                .data(LocalDate.of(2026, 11, 20))
                                .horaInicio(LocalTime.of(14, 0))
                                .horaFim(LocalTime.of(16, 0))
                                .local("Sala de reuniões")
                                .status(EventStatus.AGENDADO)
                                .createdBy(admin)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .students(Set.of())
                                .paraTodos(false)
                                .turmas(Set.of("G1", "G2"))
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião Geral",
                                "Evento aberto a todos os responsáveis",
                                LocalDate.of(2026, 11, 25),
                                LocalTime.of(14, 0),
                                LocalTime.of(16, 0),
                                "Auditório",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of("G1", "G2") // turmas antigas enviadas
                );

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(eventoExistente));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                EventResponse response = eventService.update(100L, request);

                // Assert
                assertEquals(100L, response.id());
                assertEquals(true, response.paraTodos());
                assertEquals(Set.of(), response.turmas());

                assertEquals(true, eventoExistente.getParaTodos());
                assertEquals(Set.of(), eventoExistente.getTurmas());

                verify(studentRepository, never()).findDistinctTurmas();
                verify(eventRepository).save(eventoExistente);
        }

        @Test
        void deveLancarExcecaoAoCriarEventoComAlunoInexistente() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento comemorativo da escola",
                                LocalDate.of(2026, 10, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                "Levar garrafa de água",
                                "https://wa.me/exemplo",
                                "https://exemplo.com/banner.jpg",
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(999L), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(studentRepository.findAllById(Set.of(999L)))
                                .thenReturn(List.of());

                // Act
                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> eventService.create(request, admin));

                // Assert
                assertEquals(
                                "Um ou mais alunos informados não foram encontrados!",
                                exception.getMessage());

                verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        void deveAtualizarEventoComSucesso() {

                // Arrange
                Student alunoAntigo = Student.builder()
                                .id(1L)
                                .nome("Aluno Antigo")
                                .dataNascimento(LocalDate.of(2020, 3, 15))
                                .turma("Infantil I")
                                .build();

                Student alunoNovo = Student.builder()
                                .id(2L)
                                .nome("Aluno Novo")
                                .dataNascimento(LocalDate.of(2020, 8, 20))
                                .turma("Infantil II")
                                .build();

                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Event eventoExistente = Event.builder()
                                .id(100L)
                                .titulo("Evento Antigo")
                                .descricao("Descrição antiga")
                                .data(LocalDate.of(2026, 10, 10))
                                .horaInicio(LocalTime.of(10, 0))
                                .horaFim(LocalTime.of(12, 0))
                                .local("Local antigo")
                                .status(EventStatus.AGENDADO)
                                .createdBy(admin)
                                .createdAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                                .updatedAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                                .students(Set.of(alunoAntigo))
                                .build();

                EventRequest request = new EventRequest(
                                "Evento Atualizado",
                                "Descrição atualizada",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Novo local",
                                "Nova observação",
                                "https://wa.me/novo",
                                "https://exemplo.com/novo-banner.jpg",
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(2L), // studentIds - aluno novo
                                true, // paraTodos
                                Set.of()); // turmas

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(eventoExistente));

                when(studentRepository.findAllById(Set.of(2L)))
                                .thenReturn(List.of(alunoNovo));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                EventResponse response = eventService.update(100L, request);

                // Assert
                assertEquals(100L, response.id());
                assertEquals("Evento Atualizado", response.titulo());
                assertEquals("Descrição atualizada", response.descricao());
                assertEquals(LocalDate.of(2026, 11, 20), response.data());
                assertEquals("Novo local", response.local());
                assertEquals(1, response.students().size());

                verify(eventRepository).save(eventoExistente);

                assertEquals("Evento Atualizado", eventoExistente.getTitulo());
                assertEquals("Descrição atualizada", eventoExistente.getDescricao());
                assertEquals(LocalDate.of(2026, 11, 20), eventoExistente.getData());
                assertEquals("Novo local", eventoExistente.getLocal());

                assertEquals(1, eventoExistente.getStudents().size());
                assertEquals(alunoNovo, eventoExistente.getStudents().iterator().next());
        }

        @Test
        void deveLancarExcecaoAoAtualizarEventoInexistente() {

                // Arrange

                EventRequest request = new EventRequest(
                                "Evento Atualizado",
                                "Descrição atualizada",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Novo local",
                                "Nova observação",
                                "https://wa.me/novo",
                                "https://exemplo.com/novo-banner.jpg",
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(1L), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(eventRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act
                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> eventService.update(999L, request));

                // Assert
                assertEquals(
                                "Evento não encontrado!",
                                exception.getMessage());

                verify(eventRepository, never()).save(any(Event.class));
                verify(studentRepository, never()).findAllById(any());
        }

        @Test
        void deveBuscarEventoPorIdComSucesso() {

                // Arrange
                Event evento = Event.builder()
                                .id(100L)
                                .titulo("Festa da Escola")
                                .descricao("Evento comemorativo")
                                .data(LocalDate.of(2026, 10, 20))
                                .horaInicio(LocalTime.of(14, 0))
                                .horaFim(LocalTime.of(17, 0))
                                .status(EventStatus.AGENDADO)
                                .createdAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                                .updatedAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                                .students(Set.of())
                                .build();

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(evento));

                // Act
                EventResponse response = eventService.findById(100L);

                // Assert
                assertEquals(100L, response.id());
                assertEquals("Festa da Escola", response.titulo());
                assertEquals(EventStatus.AGENDADO, response.status());

                verify(eventRepository).findById(100L);
        }

        @Test
        void deveLancarExcecaoAoBuscarEventoInexistente() {

                // Arrange
                when(eventRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act
                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> eventService.findById(999L));

                // Assert
                assertEquals(
                                "Evento não encontrado!",
                                exception.getMessage());
        }

        @Test
        void deveExcluirEventoComSucesso() {

                // Arrange
                Event evento = Event.builder()
                                .id(100L)
                                .titulo("Festa da Escola")
                                .students(Set.of())
                                .build();

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(evento));

                // Act
                eventService.delete(100L);

                // Assert
                verify(eventRepository).findById(100L);
                verify(eventRepository).delete(evento);
        }

        @Test
        void deveLancarExcecaoQuandoHoraFimNaoForPosteriorAHoraInicio() {

                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Reunião de Pais",
                                "Reunião com os responsáveis",
                                LocalDate.of(2026, 10, 20),
                                LocalTime.of(18, 0),
                                LocalTime.of(17, 0),
                                "Auditório",
                                null,
                                null,
                                null,
                                null, // bannerMediaId
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                // Act
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> eventService.create(request, admin));

                // Assert
                assertEquals(
                                "A hora de término deve ser posterior à hora de início",
                                exception.getMessage());

                verify(eventRepository, never()).save(any(Event.class));
                verify(studentRepository, never()).findAllById(any());
        }

        @Test
        void deveLancarExcecaoAoInformarDoisTiposDeBanner() {
                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento comemorativo",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                null,
                                null,
                                "https://exemplo.com/banner.jpg",
                                1L,
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                // Act
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> eventService.create(request, admin));

                // Assert
                assertEquals(
                                "Informe apenas uma URL de banner ou uma imagem da biblioteca.",
                                exception.getMessage());

                verify(mediaRepository, never()).findById(any());
                verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        void deveCriarEventoComImagemDaBiblioteca() {
                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Media imagem = new Media();
                imagem.setId(1L);

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento comemorativo",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                null,
                                null,
                                null,
                                1L,
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(mediaRepository.findById(1L))
                                .thenReturn(Optional.of(imagem));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                EventResponse response = eventService.create(request, admin);

                // Assert
                assertEquals(1L, response.bannerMediaId());

                // Assert
                ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

                verify(eventRepository).save(eventCaptor.capture());

                Event eventoSalvo = eventCaptor.getValue();

                assertEquals(imagem, eventoSalvo.getBannerMedia());
                assertEquals(null, eventoSalvo.getBannerUrl());

                verify(mediaRepository).findById(1L);
        }

        @Test
        void deveLancarExcecaoAoCriarEventoComImagemInexistente() {
                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                EventRequest request = new EventRequest(
                                "Festa da Escola",
                                "Evento comemorativo",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                null,
                                null,
                                null,
                                999L,
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(mediaRepository.findById(999L))
                                .thenReturn(Optional.empty());

                // Act
                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> eventService.create(request, admin));

                // Assert
                assertEquals(
                                "Imagem do banner não encontrada!",
                                exception.getMessage());

                verify(mediaRepository).findById(999L);
                verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        void deveAtualizarImagemDoEventoComSucesso() {
                // Arrange
                User admin = User.builder()
                                .id(10L)
                                .nome("Administrador")
                                .build();

                Media imagemAntiga = new Media();
                imagemAntiga.setId(1L);

                Media imagemNova = new Media();
                imagemNova.setId(2L);

                Event eventoExistente = Event.builder()
                                .id(100L)
                                .titulo("Evento Antigo")
                                .descricao("Descrição antiga")
                                .data(LocalDate.of(2026, 10, 20))
                                .status(EventStatus.AGENDADO)
                                .bannerMedia(imagemAntiga)
                                .createdBy(admin)
                                .students(Set.of())
                                .build();

                EventRequest request = new EventRequest(
                                "Evento Atualizado",
                                "Descrição atualizada",
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(14, 0),
                                LocalTime.of(17, 0),
                                "Pátio da escola",
                                null,
                                null,
                                null,
                                2L,
                                EventStatus.AGENDADO,
                                Set.of(), // studentIds
                                true, // paraTodos
                                Set.of()); // turmas

                when(eventRepository.findById(100L))
                                .thenReturn(Optional.of(eventoExistente));

                when(mediaRepository.findById(2L))
                                .thenReturn(Optional.of(imagemNova));

                when(eventRepository.save(any(Event.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                eventService.update(100L, request);

                // Assert
                assertEquals(imagemNova, eventoExistente.getBannerMedia());
                assertEquals(null, eventoExistente.getBannerUrl());

                verify(mediaRepository).findById(2L);
                verify(eventRepository).save(eventoExistente);
        }

}
