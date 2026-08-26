package br.com.kenzowebstudio.agenda_virtual.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.kenzowebstudio.agenda_virtual.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

}
