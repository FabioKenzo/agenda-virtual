package br.com.kenzowebstudio.agenda_virtual.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import br.com.kenzowebstudio.agenda_virtual.model.Media;
import br.com.kenzowebstudio.agenda_virtual.model.Role;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.MediaRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaResponse;
import org.springframework.core.io.Resource;
import br.com.kenzowebstudio.agenda_virtual.exception.ResourceNotFoundException;
import br.com.kenzowebstudio.agenda_virtual.dto.MediaFileResponse;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaStorageService mediaStorageService;

    public Media upload(MultipartFile file, User admin) {

        if (admin == null || admin.getId() == null
                || admin.getRole() != Role.ADMIN
                || !Boolean.TRUE.equals(admin.getAtivo())) {
            throw new SecurityException(
                    "Apenas administradores ativos podem enviar imagens.");
        }

        String storedFilename = mediaStorageService.store(file);

        try {

            Media media = Media.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .storagePath(storedFilename)
                    .uploadedBy(admin)
                    .createdAt(LocalDateTime.now())
                    .build();

            return mediaRepository.saveAndFlush(media);

        } catch (RuntimeException exception) {

            try {
                mediaStorageService.delete(storedFilename);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> listAll() {

        return mediaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(MediaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MediaFileResponse loadFile(Long id) {

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Imagem não encontrada."));

        Resource resource = mediaStorageService.load(
                media.getStoredFilename());

        return new MediaFileResponse(
                resource,
                media.getContentType());
    }

}
