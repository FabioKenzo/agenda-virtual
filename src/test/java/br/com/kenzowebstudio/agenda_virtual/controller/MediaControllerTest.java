package br.com.kenzowebstudio.agenda_virtual.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import br.com.kenzowebstudio.agenda_virtual.exception.GlobalExceptionHandler;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.service.MediaService;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(mediaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar204AoExcluirImagem() throws Exception {

        mockMvc.perform(delete("/admin/media/10"))
                .andExpect(status().isNoContent());

        verify(mediaService).delete(10L);
    }

    @Test
    void deveRetornar404QuandoImagemNaoExistir() throws Exception {

        doThrow(new ResourceNotFoundException(
                "Imagem não encontrada."))
                .when(mediaService).delete(999L);

        mockMvc.perform(delete("/admin/media/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Imagem não encontrada."));

        verify(mediaService).delete(999L);
    }

    @Test
    void deveRetornar409QuandoImagemEstiverEmUso() throws Exception {

        doThrow(new ResourceConflictException(
                "Esta imagem está sendo utilizada como banner de um evento e não pode ser excluída."))
                .when(mediaService).delete(10L);

        mockMvc.perform(delete("/admin/media/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Esta imagem está sendo utilizada como banner de um evento e não pode ser excluída."));

        verify(mediaService).delete(10L);
    }

}
