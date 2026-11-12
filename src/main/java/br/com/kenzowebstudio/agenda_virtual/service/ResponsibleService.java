package br.com.kenzowebstudio.agenda_virtual.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleEventResponse;
import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.AnnouncementRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.EventRepository;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;
import org.springframework.transaction.annotation.Transactional;
import br.com.kenzowebstudio.agenda_virtual.model.Event;

@Service
public class ResponsibleService {

        private final StudentRepository studentRepository;
        private final EventRepository eventRepository;
        private final AnnouncementRepository announcementRepository;
        private final MediaService mediaService;

        public ResponsibleService(
                        StudentRepository studentRepository,
                        EventRepository eventRepository,
                        AnnouncementRepository announcementRepository,
                        MediaService mediaService) {

                this.studentRepository = studentRepository;
                this.eventRepository = eventRepository;
                this.announcementRepository = announcementRepository;
                this.mediaService = mediaService;
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

        @Transactional(readOnly = true)
        public List<ResponsibleEventResponse> findEvents(User user) {

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
                                                resolveBannerUrl(event),
                                                event.getStatus()))
                                .toList();
        }

        @Transactional(readOnly = true)
        public ResponsibleEventResponse findEventById(Long id, User user) {

                var event = eventRepository
                                .findDistinctByIdAndStudentsUserId(id, user.getId())
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
                                .findDistinctByIdAndStudentsUserId(eventId, user.getId())
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
