package br.com.kenzowebstudio.agenda_virtual.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.kenzowebstudio.agenda_virtual.dto.ResponsibleStudentResponse;
import br.com.kenzowebstudio.agenda_virtual.model.User;
import br.com.kenzowebstudio.agenda_virtual.repository.StudentRepository;

@Service
public class ResponsibleService {

    private final StudentRepository studentRepository;

    public ResponsibleService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
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

}
