package pl.pk.pietrzak.ogryzek;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@SpringBootApplication
@EnableJpaRepositories("pl.pk.pietrzak.ogryzek.entity")
public class PietrzakOgryzekProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PietrzakOgryzekProjectApplication.class, args);
    }

    @Bean
    CommandLineRunner testConnection(DataSource dataSource) {
        return args -> {
            try (var conn = dataSource.getConnection()) {
                System.out.println("DB CONNECTED = " + conn.isValid(2));
                System.out.println("DB URL = " + conn.getMetaData().getURL());
                System.out.println("DB PRODUCT = " + conn.getMetaData().getDatabaseProductName());
            } catch (Exception e) {
                System.out.println("DB TEST FAILED");
                e.printStackTrace();
            }
        };
    }
}