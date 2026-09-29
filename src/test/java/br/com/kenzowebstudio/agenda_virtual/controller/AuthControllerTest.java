package br.com.kenzowebstudio.agenda_virtual.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.kenzowebstudio.agenda_virtual.dto.LoginRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.StudentRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterResponse;
import br.com.kenzowebstudio.agenda_virtual.security.JwtService;
import br.com.kenzowebstudio.agenda_virtual.service.UserService;
import org.springframework.security.core.Authentication;
import br.com.kenzowebstudio.agenda_virtual.model.User;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void deveCadastrarResponsavelERetornar201() throws Exception {

        // Arrange
        StudentRegisterRequest student = new StudentRegisterRequest(
                "Aluno Teste",
                LocalDate.of(2020, 5, 10),
                "Infantil II");

        UserRegisterRequest request = new UserRegisterRequest(
                "Responsável Teste",
                "responsavel@teste.com",
                "123456",
                "12999999999",
                List.of(student));

        UserRegisterResponse serviceResponse = new UserRegisterResponse(
                10L,
                "Responsável Teste",
                "responsavel@teste.com");

        when(userService.register(request))
                .thenReturn(serviceResponse);

        // Act + Assert
        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Responsável Teste"))
                .andExpect(jsonPath("$.email")
                        .value("responsavel@teste.com"));
    }

    @Test
    void deveRetornar400QuandoCadastroForInvalido() throws Exception {

        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "",
                "email-invalido",
                "123",
                "12999999999",
                List.of());

        // Act + Assert
        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void deveRealizarLoginERetornarToken() throws Exception {

        // Arrange
        LoginRequest request = new LoginRequest(
                "responsavel@teste.com",
                "123456");

        User user = User.builder()
                .id(10L)
                .nome("Responsável Teste")
                .email("responsavel@teste.com")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("token-jwt-teste");

        // Act + Assert
        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.toke")
                        .value("token-jwt-teste"));

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService).generateToken(user);
    }
}