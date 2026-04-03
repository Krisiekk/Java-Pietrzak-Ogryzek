package pl.pk.pietrzak.ogryzek;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.CommandLineRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Testy aplikacji PietrzakOgryzekProject")
class PietrzakOgryzekProjectApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Kontekst aplikacji powinien się załadować")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("CommandLineRunner bean powinien być dostępny")
    void commandLineRunnerBeanShouldBeAvailable() {
        assertNotNull(applicationContext.getBean("testConnection", CommandLineRunner.class));
    }

    @Test
    @DisplayName("Aplikacja powinna mieć wszystkie wymagane beany")
    void applicationShouldHaveAllRequiredBeans() {
        assertTrue(applicationContext.containsBean("testConnection"));
    }

    @Test
    @DisplayName("Aplikacja powinna być typu SpringBootApplication")
    void applicationShouldBeSpringBootApplication() {
        assertNotNull(PietrzakOgryzekProjectApplication.class);
    }
}
