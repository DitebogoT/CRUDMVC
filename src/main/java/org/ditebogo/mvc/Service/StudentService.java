package org.ditebogo.mvc.Service;

import org.ditebogo.mvc.DTO.StudentRequest;
import org.ditebogo.mvc.DTO.StudentResponse;
import org.ditebogo.mvc.Entity.Students;
import org.ditebogo.mvc.Exceptions.DuplicateResourceException;
import org.ditebogo.mvc.Exceptions.ResourceNotFoundException;
import org.ditebogo.mvc.Repository.StudentRepository;
import java.util.List;

import org.hibernate.DuplicateMappingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }

// Creates the students and add them into the DB
    @Transactional
    public StudentResponse createStudent(StudentRequest request)
    {
        if (studentRepository.existsByEmail(request.email()))
        {
            throw new DuplicateResourceException("A student with this email already exists");
        }

        Students students = new Students();
        students.setFirstName(request.firstName());
        students.setLastName(request.lastName());
        students.setEmail(request.email());

        Students savedStudent = studentRepository.save(students);

        return  mapToResponse(savedStudent);
    }

    //This method reads All records from the DB
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents(){

        return studentRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    //Updates a record inside a DB
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {

        Students student = findStudentOrThrow(id);

        boolean emailUsedByAnotherStudent =
                studentRepository.existsByEmailAndIdNot(
                        request.email(),
                        id
                );

        if (emailUsedByAnotherStudent) {
            throw new DuplicateResourceException(
                    "Another student is already using this email"
            );
        }

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());

        Students updatedStudent = studentRepository.save(student);

        return mapToResponse(updatedStudent);
    }

    // DELETE
    @Transactional
    public void deleteStudent(Long id) {

        Students student = findStudentOrThrow(id);

        studentRepository.delete(student);
    }

    private Students findStudentOrThrow(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + id
                ));
    }

    private StudentResponse mapToResponse(Students student) {

        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail()
        );
    }



}
