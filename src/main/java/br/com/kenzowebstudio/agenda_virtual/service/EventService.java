package br.com.kenzowebstudio.agenda_virtual.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.kenzowebstudio.agenda_virtual.dto.EventRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.EventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.StudentResponse;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.model.Event;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private final StudentRepository studentRepository;

    public EventService(
            EventRepository eventRepository,
            StudentRepository studentRepository) {
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
    }

    public EventResponse create(EventRequest request, User user) {

        LocalDateTime now = LocalDateTime.now();
        Set<Student> students = findStudentsByIds(request.studentIds());

        Event event = Event.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .data(request.data())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .local(request.local())
                .observacoes(request.observacoes())
                .whatsappUrl(request.whatsappUrl())
                .bannerUrl(request.bannerUrl())
                .status(request.status())
                .createdBy(user)
                .createdAt(now)
                .updatedAt(now)
                .students(students)
                .build();

        Event savedEvent = eventRepository.save(event);

        return toResponse(savedEvent);
    }

    public EventResponse update(Long id, EventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado!"));

        event.setTitulo(request.titulo());
        event.setDescricao(request.descricao());
        event.setData(request.data());
        event.setHoraInicio(request.horaInicio());
        event.setHoraFim(request.horaFim());
        event.setLocal(request.local());
        event.setObservacoes(request.observacoes());
        event.setWhatsappUrl(request.whatsappUrl());
        event.setBannerUrl(request.bannerUrl());
        event.setStatus(request.status());
        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);

        return toResponse(updatedEvent);
    }

    private EventResponse toResponse(Event event) {

        Set<StudentResponse> students = event.getStudents()
                .stream()
                .map(student -> new StudentResponse(
                        student.getId(),
                        student.getNome(),
                        student.getDataNascimento(),
                        student.getTurma()))
                .collect(java.util.stream.Collectors.toSet());

        return new EventResponse(
                event.getId(),
                event.getTitulo(),
                event.getDescricao(),
                event.getData(),
                event.getHoraInicio(),
                event.getHoraFim(),
                event.getLocal(),
                event.getObservacoes(),
                event.getWhatsappUrl(),
                event.getBannerUrl(),
                event.getStatus(),
                event.getCreatedBy() != null
                        ? event.getCreatedBy().getId()
                        : null,
                event.getCreatedAt(),
                event.getUpdatedAt(),
                students);
    }

    public List<EventResponse> findAll() {

        return eventRepository.findAllByOrderByDataAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse findById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado!"));

        return toResponse(event);
    }

    public void delete(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado!"));

        eventRepository.delete(event);

    }

    private Set<Student> findStudentsByIds(Set<Long> studentIds) {

        if (studentIds == null || studentIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Student> students = studentRepository.findAllById(studentIds);

        if (students.size() != studentIds.size()) {
            throw new ResourceNotFoundException("Um ou mais alunos informados não foram encontrados!");
        }

        return new HashSet<>(students);
    }

}