package br.com.kenzowebstudio.agenda_virtual.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleProfileResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.Event;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.AnnouncementRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.kenzowebstudio.agenda_virtual.dto.UpdateResponsibleProfileRequest;
import br.com.kenzowebstudio.agenda_virtual.repository.UserRepository;
import java.util.ArrayList;
import br.com.kenzowebstudio.agenda_virtual.dto.CreateStudentsRequest;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.dto.UpdateStudentRequest;

@Service
public class ResponsibleService {

        private final StudentRepository studentRepository;
        private final EventRepository eventRepository;
        private final AnnouncementRepository announcementRepository;
        private final MediaService mediaService;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public ResponsibleService(
                        StudentRepository studentRepository,
                        EventRepository eventRepository,
                        AnnouncementRepository announcementRepository,
                        MediaService mediaService,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {

                this.studentRepository = studentRepository;
                this.eventRepository = eventRepository;
                this.announcementRepository = announcementRepository;
                this.mediaService = mediaService;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public ResponsibleProfileResponse findProfile(User user) {

                return new ResponsibleProfileResponse(
                                user.getId(),
                                user.getNome(),
                                user.getEmail(),
                                user.getTelefone(),
                                user.getRole());
        }

        @Transactional
        public ResponsibleProfileResponse updateProfile(
                        User authenticatedUser,
                        UpdateResponsibleProfileRequest request) {

                User user = userRepository.findById(authenticatedUser.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Responsável não encontrado."));

                if (!passwordEncoder.matches(
                                request.senhaAtual(), user.getPassword())) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Senha atual incorreta.");
                }

                user.setNome(request.nome().trim());

                user.setTelefone(
                                request.telefone() == null || request.telefone().isBlank()
                                                ? null
                                                : request.telefone().trim());

                user.setUpdatedAt(LocalDateTime.now());

                return new ResponsibleProfileResponse(
                                user.getId(),
                                user.getNome(),
                                user.getEmail(),
                                user.getTelefone(),
                                user.getRole());
        }

        public List<ResponsibleStudentResponse> findStudents(User user) {

                return studentRepository.findByUserId(user.getId())
                                .stream()
                                .map(student -> new ResponsibleStudentResponse(
                                                student.getId(),
                                                student.getNome(),
                                                student.getDataNascimento(),
                                                student.getTurma()))
                                .toList();
        }

        @Transactional
        public List<ResponsibleStudentResponse> createStudents(
                        User authenticatedUser,
                        CreateStudentsRequest request) {

                // Busca o responsável autenticado no banco
                User responsavel = userRepository.findById(authenticatedUser.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Responsável não encontrado."));

                // Verifica se a conta está ativa
                if (!responsavel.getAtivo()) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "A conta do responsável está inativa.");
                }

                List<Student> students = new ArrayList<>();

                for (var alunoRequest : request.alunos()) {

                        Student student = Student.builder()
                                        .nome(alunoRequest.nome().trim())
                                        .dataNascimento(alunoRequest.dataNascimento())
                                        .turma(alunoRequest.turma())
                                        .user(responsavel)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        students.add(student);
                }

                // Persiste todos os alunos na mesma transação
                List<Student> savedStudents = studentRepository.saveAll(students);

                // Converte as entidades para o DTO de resposta
                return savedStudents.stream()
                                .map(student -> new ResponsibleStudentResponse(
                                                student.getId(),
                                                student.getNome(),
                                                student.getDataNascimento(),
                                                student.getTurma()))
                                .toList();
        }

        @Transactional
        public ResponsibleStudentResponse updateStudent(
                        Long studentId,
                        User authenticatedUser,
                        UpdateStudentRequest request) {

                User responsavel = userRepository
                                .findById(authenticatedUser.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Responsável não encontrado."));

                if (!Boolean.TRUE.equals(responsavel.getAtivo())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "A conta do responsável está inativa.");
                }

                Student student = studentRepository
                                .findByIdAndUserId(studentId, responsavel.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Aluno não encontrado."));

                // Confirma a senha antes de alterar os dados
                if (!passwordEncoder.matches(
                                request.senhaAtual(),
                                responsavel.getPassword())) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Senha atual incorreta.");
                }

                student.setNome(request.nome().trim());
                student.setDataNascimento(request.dataNascimento());
                student.setTurma(request.turma());

                return new ResponsibleStudentResponse(
                                student.getId(),
                                student.getNome(),
                                student.getDataNascimento(),
                                student.getTurma());
        }

        @Transactional(readOnly = true)
        public List<ResponsibleEventResponse> findEvents(User user) {

                return eventRepository
                                .findEventsVisibleToResponsible(user.getId())
                                .stream()
                                .map(event -> new ResponsibleEventResponse(
                                                event.getId(),
                                                event.getTitulo(),
                                                event.getDescricao(),
                                                event.getData(),
                                                event.getHoraInicio(),
                                                event.getHoraFim(),
                                                event.getLocal(),
                                                event.getObservacoes(),
                                                event.getWhatsappUrl(),
                                                resolveBannerUrl(event),
                                                event.getStatus()))
                                .toList();
        }

        @Transactional(readOnly = true)
        public Page<ResponsibleEventResponse> findEventsPaginated(
                        User user,
                        Pageable pageable) {

                return eventRepository
                                .findEventsByResponsibleId(user.getId(), pageable)
                                .map(event -> new ResponsibleEventResponse(
                                                event.getId(),
                                                event.getTitulo(),
                                                event.getDescricao(),
                                                event.getData(),
                                                event.getHoraInicio(),
                                                event.getHoraFim(),
                                                event.getLocal(),
                                                event.getObservacoes(),
                                                event.getWhatsappUrl(),
                                                resolveBannerUrl(event),
                                                event.getStatus()));
        }

        @Transactional(readOnly = true)
        public ResponsibleEventResponse findEventById(Long id, User user) {

                var event = eventRepository
                                .findVisibleEventById(id, user.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Evento não encontrado"));

                return new ResponsibleEventResponse(
                                event.getId(),
                                event.getTitulo(),
                                event.getDescricao(),
                                event.getData(),
                                event.getHoraInicio(),
                                event.getHoraFim(),
                                event.getLocal(),
                                event.getObservacoes(),
                                event.getWhatsappUrl(),
                                resolveBannerUrl(event),
                                event.getStatus());
        }

        public List<AnnouncementResponse> findAnnouncements() {

                return announcementRepository
                                .findByAtivoTrueOrderByCreatedAtDesc()
                                .stream()
                                .map(announcement -> new AnnouncementResponse(
                                                announcement.getId(),
                                                announcement.getTitulo(),
                                                announcement.getConteudo(),
                                                announcement.getAtivo(),
                                                announcement.getCreatedBy() != null
                                                                ? announcement.getCreatedBy().getId()
                                                                : null,
                                                announcement.getCreatedAt(),
                                                announcement.getUpdatedAt()))
                                .toList();
        }

        @Transactional(readOnly = true)
        public Page<AnnouncementResponse> findAnnouncementsPaginated(
                        Pageable pageable) {

                return announcementRepository
                                .findByAtivoTrueOrderByCreatedAtDesc(pageable)
                                .map(announcement -> new AnnouncementResponse(
                                                announcement.getId(),
                                                announcement.getTitulo(),
                                                announcement.getConteudo(),
                                                announcement.getAtivo(),
                                                announcement.getCreatedBy() != null
                                                                ? announcement.getCreatedBy().getId()
                                                                : null,
                                                announcement.getCreatedAt(),
                                                announcement.getUpdatedAt()));
        }

        public AnnouncementResponse findAnnouncementById(Long id) {

                var announcement = announcementRepository
                                .findByIdAndAtivoTrue(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Comunicado não encontrado"));

                return new AnnouncementResponse(
                                announcement.getId(),
                                announcement.getTitulo(),
                                announcement.getConteudo(),
                                announcement.getAtivo(),
                                announcement.getCreatedBy() != null
                                                ? announcement.getCreatedBy().getId()
                                                : null,
                                announcement.getCreatedAt(),
                                announcement.getUpdatedAt());
        }

        @Transactional(readOnly = true)
        public MediaFileResponse findEventBanner(Long eventId, User user) {

                var event = eventRepository
                                .findVisibleEventById(eventId, user.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Evento não encontrado."));

                if (event.getBannerMedia() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Este evento não possui uma imagem da biblioteca.");
                }

                return mediaService.loadFile(event.getBannerMedia().getId());
        }

        private String resolveBannerUrl(Event event) {

                if (event.getBannerMedia() != null) {
                        return "http://localhost:8080/responsavel/eventos/"
                                        + event.getId()
                                        + "/banner";
                }

                return event.getBannerUrl();
        }
}
