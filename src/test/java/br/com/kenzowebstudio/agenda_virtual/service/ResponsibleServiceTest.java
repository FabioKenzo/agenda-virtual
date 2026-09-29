package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@ExtendWith(MockitoExtension.class)
class ResponsibleServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EventRepository eventRepository;

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
                .findDistinctByStudentsUserIdOrderByDataAsc(10L))
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
                .findDistinctByStudentsUserIdOrderByDataAsc(10L);
    }
}