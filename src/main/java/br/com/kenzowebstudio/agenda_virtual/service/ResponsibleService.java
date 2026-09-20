package br.com.kenzowebstudio.agenda_virtual.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;

@Service
public class ResponsibleService {

    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    public ResponsibleService(
            StudentRepository studentRepository,
            EventRepository eventRepository) {

        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;

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

    public List<ResponsibleEventResponse> findEvents(User user){

        return eventRepository.findDistinctByStudentsUserIdOrderByDataAsc(user.getId())
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
                    event.getBannerUrl(),
                    event.getStatus()
            )).toList();
    }
}
