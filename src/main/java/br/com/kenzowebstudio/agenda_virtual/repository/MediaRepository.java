package br.com.kenzowebstudio.agenda_virtual.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.kenzowebstudio.agenda_virtual.model.Media;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findAllByOrderByCreatedAtDesc();

}
