package br.com.kenzowebstudio.agenda_virtual.controller;

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
import org.springframework.http.ResponseEntity;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.EventStatus;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.service.ResponsibleService;

@ExtendWith(MockitoExtension.class)
class ResponsibleControllerTest {

    @Mock
    private ResponsibleService responsibleService;

    @InjectMocks
    private ResponsibleController responsibleController;

    @Test
    void deveRetornarAlunosDoResponsavel() {

        // Arrange
        User responsavel = User.builder()
                .id(10L)
                .nome("Responsável Teste")
                .email("responsavel@teste.com")
                .build();

        ResponsibleStudentResponse aluno = new ResponsibleStudentResponse(
                1L,
                "Aluno Teste",
                LocalDate.of(2020, 5, 10),
                "Infantil II");

        when(responsibleService.findStudents(responsavel))
                .thenReturn(List.of(aluno));

        // Act
        ResponseEntity<List<ResponsibleStudentResponse>> response = responsibleController.findStudents(responsavel);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        assertEquals(
                "Aluno Teste",
                response.getBody().get(0).nome());

        assertEquals(
                "Infantil II",
                response.getBody().get(0).turma());

        verify(responsibleService)
                .findStudents(responsavel);
    }

    @Test
    void deveRetornarEventosDoResponsavel() {

        // Arrange
        User responsavel = User.builder()
                .id(10L)
                .nome("Responsável Teste")
                .email("responsavel@teste.com")
                .build();

        ResponsibleEventResponse evento = new ResponsibleEventResponse(
                100L,
                "Festa da Escola",
                "Evento comemorativo",
                LocalDate.of(2026, 10, 20),
                LocalTime.of(14, 0),
                LocalTime.of(17, 0),
                "Pátio da escola",
                "Levar garrafa de água",
                "https://wa.me/exemplo",
                "https://exemplo.com/banner.jpg",
                EventStatus.AGENDADO);

        when(responsibleService.findEvents(responsavel))
                .thenReturn(List.of(evento));

        // Act
        ResponseEntity<List<ResponsibleEventResponse>> response = responsibleController.findeEvents(responsavel);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        assertEquals(
                "Festa da Escola",
                response.getBody().get(0).titulo());

        assertEquals(
                EventStatus.AGENDADO,
                response.getBody().get(0).status());

        verify(responsibleService)
                .findEvents(responsavel);
    }
}
