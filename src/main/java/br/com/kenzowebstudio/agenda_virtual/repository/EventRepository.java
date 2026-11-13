package br.com.kenzowebstudio.agenda_virtual.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.kenzowebstudio.agenda_virtual.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByDataAsc();

    List<Event> findDistinctByStudentsUserIdOrderByDataAsc(Long userId);

    Optional<Event> findDistinctByIdAndStudentsUserId(
            Long id,
            Long userId);

    boolean existsByBannerMediaId(Long mediaId);

    @Query(value = """
            SELECT DISTINCT e
            FROM Event e
            JOIN e.students s
            WHERE s.user.id = :userId
            ORDER BY e.data ASC, e.id ASC
            """, countQuery = """
            SELECT COUNT(DISTINCT e.id)
            FROM Event e
            JOIN e.students s
            WHERE s.user.id = :userId
            """)
    Page<Event> findEventsByResponsibleId(
            @Param("userId") Long userId,
            Pageable pageable);
}
