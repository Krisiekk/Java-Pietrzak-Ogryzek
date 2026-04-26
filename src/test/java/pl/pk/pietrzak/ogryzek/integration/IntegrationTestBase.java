package pl.pk.pietrzak.ogryzek.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Klasa bazowa dla testów integracyjnych bez Dockera/TestContainers.
 *
 * Testy korzystają z profilu "test" i mają automatycznie skonfigurowany MockMvc.
 * Konfiguracja bazy danych powinna znajdować się w:
 * src/test/resources/application-test.properties.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
}
