package br.com.kenzowebstudio.agenda_virtual.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.kenzowebstudio.agenda_virtual.model.Student;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByUserId(Long userId);

    @Query("""
            SELECT DISTINCT s.turma
            FROM Student s
            WHERE s.turma IS NOT NULL
              AND TRIM(s.turma) <> ''
            """)
    Set<String> findDistinctTurmas();

}
