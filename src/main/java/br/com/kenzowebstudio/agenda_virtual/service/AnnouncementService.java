package br.com.kenzowebstudio.agenda_virtual.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementRequest;
import br.com.kenzowebstudio.agenda_virtual.dto.AnnouncementResponse;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.model.Announcement;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.AnnouncementRepository;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {

        this.announcementRepository = announcementRepository;
    }

    public AnnouncementResponse create(AnnouncementRequest request, User user) {

        LocalDateTime now = LocalDateTime.now();

        Announcement announcement = Announcement.builder()
                .titulo(request.titulo())
                .conteudo(request.conteudo())
                .ativo(request.ativo())
                .createdBy(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        return toResponse(savedAnnouncement);

    }

    private AnnouncementResponse toResponse(Announcement announcement) {

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

    public List<AnnouncementResponse> findAll() {

        return announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AnnouncementResponse findById(Long id) {

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() ->  new ResourceNotFoundException("Comunicado não encontrado!"));

        return toResponse(announcement);

    }

    public AnnouncementResponse update(Long id, AnnouncementRequest request) {

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() ->  new ResourceNotFoundException("Comunicado não encontrado!"));

        announcement.setTitulo(request.titulo());
        announcement.setConteudo(request.conteudo());
        announcement.setAtivo(request.ativo());
        announcement.setUpdatedAt(LocalDateTime.now());

        Announcement updatedAnnouncement = announcementRepository.save(announcement);

        return toResponse(updatedAnnouncement);
    }

    public void delete(Long id) {

    Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() ->  new ResourceNotFoundException("Comunicado não encontrado!"));

    announcementRepository.delete(announcement);
}

}
