package de.pathiful.calculator.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registriert den {@link CalculatorService} als Spring-Bean, ohne ihn mit
 * Framework-Annotationen zu belasten. Der Service selbst bleibt ein reines POJO.
 */
@Configuration
public class CalculatorServiceConfig {

    @Bean
    public CalculatorService calculatorService() {
        return new CalculatorService();
    }
}
