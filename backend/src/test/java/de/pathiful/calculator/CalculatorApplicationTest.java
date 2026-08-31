package de.pathiful.calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Kontext-Test fuer die Spring-Boot-Anwendung, abgeleitet aus den
 * Akzeptanzkriterien des Issues #88: Der Application-Context startet, damit die
 * Startklasse {@link CalculatorApplication} und die Bean-Registrierung
 * ({@code CalculatorServiceConfig}) abgedeckt sind.
 */
@SpringBootTest
class CalculatorApplicationTest {

    @Test
    @DisplayName("Application-Context startet erfolgreich")
    void contextLoads() {
        // Failt automatisch, wenn der Kontext nicht hochkommt.
    }
}
