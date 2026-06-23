package com.pathiful.payment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die Bean-Validation-Annotationen auf SepaMandateRequest.
 *
 * Deckt die Akzeptanzkriterien ab:
 * - fehlende Zustimmung (acceptedTerms = false)
 * - ungültige IBAN (falsches Format)
 * - gültige Daten (happy path)
 */
class SepaMandateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassForValidRequest() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89370400440532013000");
        request.setBic("COBADEFFXXX");
        request.setBankName("Commerzbank");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Es sollten keine Validierungsfehler auftreten, wurde: " + violations);
    }

    @Test
    void shouldRejectMissingAccountHolder() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setIban("DE89370400440532013000");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accountHolder")));
    }

    @Test
    void shouldRejectMissingIban() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("iban")));
    }

    @Test
    void shouldRejectInvalidIbanFormat() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("not-an-iban");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("iban")
                        && v.getMessage().contains("IBAN")));
    }

    @Test
    void shouldRejectMissingTermsAcceptance() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89370400440532013000");
        request.setAcceptedTerms(false);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Zustimmung")));
    }

    @Test
    void shouldPassForIbanWithSpaces() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89 3704 0044 0532 0130 00");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "IBAN mit Leerzeichen sollte gültig sein, wurde: " + violations);
    }

    @Test
    void shouldRejectTooShortIban() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE12");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptBicAsOptional() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89370400440532013000");
        request.setBankName("Testbank");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldAcceptBankNameAsOptional() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89370400440532013000");
        request.setBic("COBADEFFXXX");
        request.setAcceptedTerms(true);

        Set<ConstraintViolation<SepaMandateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
