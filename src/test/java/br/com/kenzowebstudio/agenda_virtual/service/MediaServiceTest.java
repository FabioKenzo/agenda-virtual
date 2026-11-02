package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import br.com.kenzowebstudio.agenda_virtual.model.Media;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.MediaRepository;
import java.util.Optional;
import org.springframework.core.io.ByteArrayResource;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceConflictException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

        @Mock
        private MediaRepository mediaRepository;

        @Mock
        private EventRepository eventRepository;

        @Mock
        private MediaStorageService mediaStorageService;

        @InjectMocks
        private MediaService mediaService;

        private User admin;
        private MockMultipartFile file;

        @BeforeEach
        void setUp() {

                admin = User.builder()
                                .id(1L)
                                .nome("Administrador")
                                .role(Role.ADMIN)
                                .ativo(true)
                                .build();

                file = new MockMultipartFile(
                                "file",
                                "banner.png",
                                "image/png",
                                new byte[] { 1, 2, 3 });
        }

        @Test
        void devePermitirUploadParaAdministrador() {

                when(mediaStorageService.store(file))
                                .thenReturn("imagem-teste.png");

                when(mediaRepository.saveAndFlush(any(Media.class)))
                                .thenAnswer(invocation -> {
                                        Media media = invocation.getArgument(0);
                                        media.setId(10L);
                                        return media;
                                });

                Media result = mediaService.upload(file, admin);

                assertEquals(10L, result.getId());
                assertEquals("banner.png", result.getOriginalFilename());
                assertEquals("imagem-teste.png", result.getStoredFilename());
                assertEquals(admin, result.getUploadedBy());

                verify(mediaStorageService).store(file);
                verify(mediaRepository).saveAndFlush(any(Media.class));
        }

        @Test
        void deveBloquearUploadParaResponsavel() {

                User responsavel = User.builder()
                                .id(2L)
                                .role(Role.RESPONSAVEL)
                                .ativo(true)
                                .build();

                assertThrows(
                                SecurityException.class,
                                () -> mediaService.upload(file, responsavel));

                verifyNoInteractions(
                                mediaStorageService,
                                mediaRepository);
        }

        @Test
        void deveExcluirArquivoQuandoBancoFalhar() {

                when(mediaStorageService.store(file))
                                .thenReturn("imagem-teste.png");

                when(mediaRepository.saveAndFlush(any(Media.class)))
                                .thenThrow(new RuntimeException(
                                                "Falha ao salvar no banco de dados."));

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> mediaService.upload(file, admin));

                assertEquals(
                                "Falha ao salvar no banco de dados.",
                                exception.getMessage());

                verify(mediaStorageService)
                                .delete("imagem-teste.png");
        }

        @Test
        void deveCarregarImagemPeloId() {

                Media media = Media.builder()
                                .id(10L)
                                .storedFilename("imagem-armazenada.png")
                                .contentType("image/png")
                                .build();

                byte[] imageBytes = new byte[] { 1, 2, 3, 4 };

                ByteArrayResource resource = new ByteArrayResource(imageBytes);

                when(mediaRepository.findById(10L))
                                .thenReturn(Optional.of(media));

                when(mediaStorageService.load("imagem-armazenada.png"))
                                .thenReturn(resource);

                MediaFileResponse response = mediaService.loadFile(10L);

                assertEquals("image/png", response.contentType());
                assertSame(resource, response.resource());

                verify(mediaRepository).findById(10L);
                verify(mediaStorageService).load("imagem-armazenada.png");
        }

        @Test
        void deveLancarExcecaoQuandoImagemNaoExistir() {

                when(mediaRepository.findById(999L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> mediaService.loadFile(999L));

                assertEquals(
                                "Imagem não encontrada.",
                                exception.getMessage());

                verify(mediaRepository).findById(999L);
                verifyNoInteractions(mediaStorageService);
        }

        @Test
        void deveBloquearExclusaoDeImagemUtilizadaEmEvento() {

                Media media = Media.builder()
                                .id(10L)
                                .storedFilename("imagem-teste.png")
                                .build();

                when(mediaRepository.findById(10L))
                                .thenReturn(Optional.of(media));

                when(eventRepository.existsByBannerMediaId(10L))
                                .thenReturn(true);

                ResourceConflictException exception = assertThrows(
                                ResourceConflictException.class,
                                () -> mediaService.delete(10L));

                assertEquals(
                                "Esta imagem está sendo utilizada como banner de um evento e não pode ser excluída.",
                                exception.getMessage());

                verify(mediaRepository).findById(10L);
                verify(eventRepository).existsByBannerMediaId(10L);

                verify(mediaStorageService, never())
                                .delete(any());

                verify(mediaRepository, never())
                                .delete(any(Media.class));
        }

        @Test
        void deveImpedirExclusaoDeImagemInexistente() {

                when(mediaRepository.findById(999L))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> mediaService.delete(999L));

                assertEquals(
                                "Imagem não encontrada.",
                                exception.getMessage());

                verify(mediaRepository).findById(999L);

                verifyNoInteractions(
                                eventRepository,
                                mediaStorageService);
        }

        @Test
        void deveExcluirArquivoSomenteAposCommit() {

                Media media = Media.builder()
                                .id(10L)
                                .storedFilename("imagem-teste.png")
                                .build();

                when(mediaRepository.findById(10L))
                                .thenReturn(Optional.of(media));

                when(eventRepository.existsByBannerMediaId(10L))
                                .thenReturn(false);

                TransactionSynchronizationManager.initSynchronization();

                try {
                        mediaService.delete(10L);

                        verify(mediaRepository).delete(media);
                        verify(mediaRepository).flush();

                        verify(mediaStorageService, never())
                                        .delete(anyString());

                        var synchronizations = TransactionSynchronizationManager.getSynchronizations();

                        assertEquals(1, synchronizations.size());

                        TransactionSynchronization synchronization = synchronizations.get(0);

                        synchronization.afterCommit();

                        verify(mediaStorageService)
                                        .delete("imagem-teste.png");

                } finally {
                        TransactionSynchronizationManager.clearSynchronization();
                }
        }

        @Test
        void naoDeveExcluirArquivoQuandoTransacaoSofrerRollback() {

                Media media = Media.builder()
                                .id(10L)
                                .storedFilename("imagem-teste.png")
                                .build();

                when(mediaRepository.findById(10L))
                                .thenReturn(Optional.of(media));

                TransactionSynchronizationManager.initSynchronization();

                try {
                        mediaService.delete(10L);

                        var synchronizations = TransactionSynchronizationManager.getSynchronizations();

                        assertEquals(1, synchronizations.size());

                        TransactionSynchronization synchronization = synchronizations.get(0);

                        synchronization.afterCompletion(
                                        TransactionSynchronization.STATUS_ROLLED_BACK);

                        verify(mediaRepository).delete(media);
                        verify(mediaRepository).flush();

                        verify(mediaStorageService, never())
                                        .delete(anyString());

                } finally {
                        TransactionSynchronizationManager.clearSynchronization();
                }
        }

}
