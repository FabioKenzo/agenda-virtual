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

   @Query("""
         SELECT e
         FROM Event e
         WHERE e.paraTodos = true
            OR EXISTS (
                SELECT s.id
                FROM Student s
                WHERE s.user.id = :userId
                  AND s.turma MEMBER OF e.turmas
            )
         ORDER BY e.data ASC, e.id ASC
         """)
   List<Event> findEventsVisibleToResponsible(
         @Param("userId") Long userId);

   @Query("""
         SELECT e
         FROM Event e
         WHERE e.id = :eventId
           AND (
               e.paraTodos = true
               OR EXISTS (
                   SELECT s.id
                   FROM Student s
                   WHERE s.user.id = :userId
                     AND s.turma MEMBER OF e.turmas
               )
           )
         """)
   Optional<Event> findVisibleEventById(
         @Param("eventId") Long eventId,
         @Param("userId") Long userId);

   boolean existsByBannerMediaId(Long mediaId);

   @Query(value = """
         SELECT e
         FROM Event e
         WHERE e.paraTodos = true
            OR EXISTS (
                SELECT s.id
                FROM Student s
                WHERE s.user.id = :userId
                  AND s.turma MEMBER OF e.turmas
            )
         ORDER BY e.data ASC, e.id ASC
         """, countQuery = """
         SELECT COUNT(e)
         FROM Event e
         WHERE e.paraTodos = true
            OR EXISTS (
                SELECT s.id
                FROM Student s
                WHERE s.user.id = :userId
                  AND s.turma MEMBER OF e.turmas
            )
         """)
   Page<Event> findEventsByResponsibleId(
         @Param("userId") Long userId,
         Pageable pageable);

}
