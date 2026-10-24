package bibliotech.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada da aplicação web (Spring Boot). */
@SpringBootApplication(scanBasePackages = "bibliotech.web")
public class BibliotechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotechApplication.class, args);
    }
}
