package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.model.Announcement;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.AnnouncementRepository;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    void deveCriarComunicadoComSucesso() {

        // Arrange
        User admin = User.builder()
                .id(10L)
                .nome("Administrador")
                .build();

        AnnouncementRequest request = new AnnouncementRequest(
                "Reunião de Pais",
                "A reunião acontecerá na próxima sexta-feira.",
                true);

        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(invocation -> {
                    Announcement announcement = invocation.getArgument(0);
                    announcement.setId(100L);
                    return announcement;
                });

        // Act
        AnnouncementResponse response = announcementService.create(request, admin);

        // Assert
        assertEquals(100L, response.id());
        assertEquals("Reunião de Pais", response.titulo());
        assertEquals(
                "A reunião acontecerá na próxima sexta-feira.",
                response.conteudo());
        assertEquals(true, response.ativo());
        assertEquals(10L, response.createdBy());

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);

        verify(announcementRepository).save(captor.capture());

        Announcement capturedAnnouncement = captor.getValue();

        assertEquals("Reunião de Pais", capturedAnnouncement.getTitulo());
        assertEquals(
                "A reunião acontecerá na próxima sexta-feira.",
                capturedAnnouncement.getConteudo());
        assertEquals(true, capturedAnnouncement.getAtivo());
        assertEquals(admin, capturedAnnouncement.getCreatedBy());
    }

    @Test
    void deveBuscarComunicadoPorIdComSucesso() {

        // Arrange
        Announcement announcement = Announcement.builder()
                .id(100L)
                .titulo("Reunião de Pais")
                .conteudo("Comunicado importante")
                .ativo(true)
                .createdAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                .build();

        when(announcementRepository.findById(100L))
                .thenReturn(Optional.of(announcement));

        // Act
        AnnouncementResponse response = announcementService.findById(100L);

        // Assert
        assertEquals(100L, response.id());
        assertEquals("Reunião de Pais", response.titulo());
        assertEquals("Comunicado importante", response.conteudo());
        assertEquals(true, response.ativo());

        verify(announcementRepository).findById(100L);
    }

    @Test
    void deveLancarExcecaoAoBuscarComunicadoInexistente() {

        // Arrange
        when(announcementRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> announcementService.findById(999L));

        // Assert
        assertEquals(
                "Comunicado não encontrado!",
                exception.getMessage());
    }

    @Test
    void deveAtualizarComunicadoComSucesso() {

        // Arrange
        User admin = User.builder()
                .id(10L)
                .nome("Administrador")
                .build();

        Announcement announcement = Announcement.builder()
                .id(100L)
                .titulo("Título antigo")
                .conteudo("Conteúdo antigo")
                .ativo(true)
                .createdBy(admin)
                .createdAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 9, 1, 10, 0))
                .build();

        AnnouncementRequest request = new AnnouncementRequest(
                "Título atualizado",
                "Conteúdo atualizado",
                false);

        when(announcementRepository.findById(100L))
                .thenReturn(Optional.of(announcement));

        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AnnouncementResponse response = announcementService.update(100L, request);

        // Assert
        assertEquals(100L, response.id());
        assertEquals("Título atualizado", response.titulo());
        assertEquals("Conteúdo atualizado", response.conteudo());
        assertEquals(false, response.ativo());

        verify(announcementRepository).save(announcement);

        assertEquals("Título atualizado", announcement.getTitulo());
        assertEquals("Conteúdo atualizado", announcement.getConteudo());
        assertEquals(false, announcement.getAtivo());
    }

    @Test
    void deveLancarExcecaoAoAtualizarComunicadoInexistente() {

        // Arrange
        AnnouncementRequest request = new AnnouncementRequest(
                "Título atualizado",
                "Conteúdo atualizado",
                true);

        when(announcementRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> announcementService.update(999L, request));

        // Assert
        assertEquals(
                "Comunicado não encontrado!",
                exception.getMessage());

        verify(announcementRepository, never())
                .save(any(Announcement.class));
    }

    @Test
    void deveExcluirComunicadoComSucesso() {

        // Arrange
        Announcement announcement = Announcement.builder()
                .id(100L)
                .titulo("Comunicado")
                .conteudo("Conteúdo")
                .ativo(true)
                .build();

        when(announcementRepository.findById(100L))
                .thenReturn(Optional.of(announcement));

        // Act
        announcementService.delete(100L);

        // Assert
        verify(announcementRepository).findById(100L);
        verify(announcementRepository).delete(announcement);
    }
}
