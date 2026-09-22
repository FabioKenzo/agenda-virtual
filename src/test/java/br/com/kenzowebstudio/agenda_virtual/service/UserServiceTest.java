package br.com.kenzowebstudio.agenda_virtual.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.kenzowebstudio.agenda_virtual.dto.StudentRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterResponse;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void deveCadastrarResponsavelComSucesso() {

        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "Fabio Teste",
                "fabio@teste.com",
                "123456",
                "12999999999",
                List.of());

        when(userRepository.findByEmail("fabio@teste.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("senha-criptografada");

        User savedUser = User.builder()
                .id(1L)
                .nome("Fabio Teste")
                .email("fabio@teste.com")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        UserRegisterResponse response = userService.register(request);

        // Assert
        assertEquals(1L, response.id());
        assertEquals("Fabio Teste", response.nome());
        assertEquals("fabio@teste.com", response.email());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {

        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "Fabio Teste",
                "fabio@teste.com",
                "123456",
                "12999999999",
                List.of());

        User existingUser = User.builder()
                .id(1L)
                .email("fabio@teste.com")
                .build();

        when(userRepository.findByEmail("fabio@teste.com"))
                .thenReturn(Optional.of(existingUser));

        // Act / Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request));

        assertEquals("Email já cadastrado", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveCriarResponsavelComSenhaCodificadaERoleCorreta() {

        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "Fabio Teste",
                "fabio@teste.com",
                "123456",
                "12999999999",
                List.of());

        when(userRepository.findByEmail("fabio@teste.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("senha-criptografada");

        User savedUser = User.builder()
                .id(1L)
                .nome("Fabio Teste")
                .email("fabio@teste.com")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        assertEquals("Fabio Teste", capturedUser.getNome());
        assertEquals("fabio@teste.com", capturedUser.getEmail());
        assertEquals("12999999999", capturedUser.getTelefone());

        assertEquals("senha-criptografada", capturedUser.getSenha());

        assertEquals(Role.RESPONSAVEL, capturedUser.getRole());

        assertTrue(capturedUser.getAtivo());
    }

    @Test
    void deveCadastrarAlunoVinculadoAoResponsavel() {

        // Arrange
        StudentRegisterRequest studentRequest = new StudentRegisterRequest(
                "Aluno Teste",
                LocalDate.of(2020, 5, 10),
                "Infantil II");

        UserRegisterRequest request = new UserRegisterRequest(
                "Fabio Teste",
                "fabio@teste.com",
                "123456",
                "12999999999",
                List.of(studentRequest));

        when(userRepository.findByEmail("fabio@teste.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("senha-criptografada");

        User savedUser = User.builder()
                .id(1L)
                .nome("Fabio Teste")
                .email("fabio@teste.com")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);

        verify(studentRepository).save(studentCaptor.capture());

        Student capturedStudent = studentCaptor.getValue();

        assertEquals("Aluno Teste", capturedStudent.getNome());
        assertEquals(
                LocalDate.of(2020, 5, 10),
                capturedStudent.getDataNascimento());
        assertEquals("Infantil II", capturedStudent.getTurma());
        assertEquals(savedUser, capturedStudent.getUser());
    }

}
