package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import tn.esprit.studentmanagement.services.StudentService;
import tn.esprit.studentmanagement.repositories.StudentRepository;

@SpringBootTest
class StudentManagementApplicationTests {

    @Autowired
    private StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    @Test
    void testGetAllStudents() {

        when(studentRepository.findAll()).thenReturn(List.of());

        List<?> students = studentService.getAllStudents();

        assertNotNull(students);
    }
}
