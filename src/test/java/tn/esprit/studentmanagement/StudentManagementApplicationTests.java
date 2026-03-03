package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Import nécessaire

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    @Test
    void testGetAllStudents() {

        when(studentRepository.findAll()).thenReturn(List.of());

        List<Student> students = studentService.getAllStudents();

        assertNotNull(students);
    }
}
