package ar.edu.utn.frc.dsi.bolsines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * PPAI DSI 2026 - Bolsines.
 * CU 36 Consultar seguimiento de bolsines, rediseñado con el patrón Adapter (GoF).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BolsinesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BolsinesApplication.class, args);
    }
}
