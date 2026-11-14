package br.com.kenzowebstudio.agenda_virtual.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.kenzowebstudio.agenda_virtual.model.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();

    List<Announcement> findByAtivoTrueOrderByCreatedAtDesc();

    Page<Announcement> findByAtivoTrueOrderByCreatedAtDesc(Pageable pageable);

    Optional<Announcement> findByIdAndAtivoTrue(Long id);
}
