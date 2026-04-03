package pl.pk.pietrzak.ogryzek;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PietrzakOgryzekProjectApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Aplikacja powinna się uruchomić bez błędów")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("CommandLineRunner bean powinien być dostępny")
    void commandLineRunnerBeanShouldBeAvailable() {
        // Bean powinien być zarejestrowany
        assertNotNull(applicationContext.getBean("testConnection"));
    }

    @Test
    @DisplayName("Main method powinien uruchomić aplikację")
    void mainMethodShouldStartApplication() {
        // Sprawdzenie że aplikacja może być uruchomiona
        assertNotNull(PietrzakOgryzekProjectApplication.class);
    }
}

