package br.com.kenzowebstudio.agenda_virtual.service;

import java.time.LocalDateTime;


import br.com.kenzowebstudio.agenda_virtual.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.kenzowebstudio.agenda_virtual.dto.StudentRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.UserRegisterResponse;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder
        ){
            this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        }

    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User user = User.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .telefone(request.telefone())
                .role(Role.RESPONSAVEL)
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        if (request.students() != null) {
            for (StudentRegisterRequest studentRequest : request.students()) {

                Student student = Student.builder()
                        .nome(studentRequest.nome())
                        .dataNascimento(studentRequest.dataNascimento())
                        .turma(studentRequest.turma())
                        .user(savedUser)
                        .createdAt(LocalDateTime.now())
                        .build();

                studentRepository.save(student);
            }
        }

        return new UserRegisterResponse(
            savedUser.getId(),
            savedUser.getNome(),
            savedUser.getEmail()
        );
    }

}
