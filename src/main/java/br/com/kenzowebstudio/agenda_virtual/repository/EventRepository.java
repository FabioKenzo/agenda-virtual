package br.com.kenzowebstudio.agenda_virtual.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.kenzowebstudio.agenda_virtual.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findAllByOrderByDataAsc();
    List<Event> findDistinctByStudentsUserIdOrderByDataAsc(Long userId);
     Optional<Event> findDistinctByIdAndStudentsUserId(Long id, Long userId);
}
