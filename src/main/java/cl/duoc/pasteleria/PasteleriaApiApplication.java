package cl.duoc.pasteleria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de arranque de Spring Boot: escanea todos los paquetes hijos de cl.duoc.pasteleria
@SpringBootApplication
public class PasteleriaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PasteleriaApiApplication.class, args);
    }
}