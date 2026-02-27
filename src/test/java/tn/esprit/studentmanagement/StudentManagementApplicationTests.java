package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Import nécessaire

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test") // <--- C'est ici que la magie opère !
class StudentManagementApplicationTests {

    @Test
    void contextLoads() {
        // J'ai corrigé l'assertion : assertNotNull(null) échouera toujours.
        // On utilise assertTrue(true) juste pour vérifier que le contexte Spring démarre.
        assertTrue(true); 
    }
}
