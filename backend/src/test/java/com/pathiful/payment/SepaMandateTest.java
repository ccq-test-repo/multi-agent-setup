package com.pathiful.payment;

import static org.junit.jupiter.api.Assertions.*;

import com.pathiful.user.User;
import org.junit.jupiter.api.Test;

class SepaMandateTest {

    @Test
    void shouldSetAndGetProperties() {
        User user = new User("payer@example.com", "hash", User.Role.USER);
        SepaMandate mandate = new SepaMandate();

        mandate.setUser(user);
        mandate.setMandateReference("MANDATE-001");
        mandate.setAccountHolderName("Max Mustermann");
        mandate.setIbanMasked("DE89 **** **** **** 1234");
        mandate.setIbanEncrypted("encrypted-iban-value");
        mandate.setBic("BELADEBEXXX");
        mandate.setBankName("Musterbank");
        mandate.setAcceptedTerms(true);
        mandate.setAcceptedAt(java.time.LocalDateTime.now());
        mandate.setStatus(SepaMandate.SepaMandateStatus.ACTIVE);

        assertNull(mandate.getId());
        assertSame(user, mandate.getUser());
        assertEquals("MANDATE-001", mandate.getMandateReference());
        assertEquals("Max Mustermann", mandate.getAccountHolderName());
        assertEquals("DE89 **** **** **** 1234", mandate.getIbanMasked());
        assertEquals("encrypted-iban-value", mandate.getIbanEncrypted());
        assertEquals("BELADEBEXXX", mandate.getBic());
        assertEquals("Musterbank", mandate.getBankName());
        assertTrue(mandate.isAcceptedTerms());
        assertEquals(SepaMandate.SepaMandateStatus.ACTIVE, mandate.getStatus());
    }

    @Test
    void shouldDefaultStatusToDraftOnPrePersist() {
        SepaMandate mandate = new SepaMandate();
        assertNull(mandate.getStatus());

        mandate.onCreate();

        assertEquals(SepaMandate.SepaMandateStatus.DRAFT, mandate.getStatus());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        SepaMandate mandate = new SepaMandate();
        assertNull(mandate.getCreatedAt());

        mandate.onCreate();

        assertNotNull(mandate.getCreatedAt());
    }

    @Test
    void shouldSetDefaultsAndNotOverrideExplicitStatus() {
        SepaMandate mandate = new SepaMandate();
        mandate.setStatus(SepaMandate.SepaMandateStatus.ACTIVE);
        mandate.onCreate();

        assertEquals(SepaMandate.SepaMandateStatus.ACTIVE, mandate.getStatus(),
                "@PrePersist should not override an explicitly set status");
    }

    @Test
    void shouldSupportAllMandateStatuses() {
        assertEquals(3, SepaMandate.SepaMandateStatus.values().length);
        assertNotNull(SepaMandate.SepaMandateStatus.valueOf("DRAFT"));
        assertNotNull(SepaMandate.SepaMandateStatus.valueOf("ACTIVE"));
        assertNotNull(SepaMandate.SepaMandateStatus.valueOf("REVOKED"));
    }

    @Test
    void shouldAllowOptionalFieldsToBeNull() {
        SepaMandate mandate = new SepaMandate();
        assertNull(mandate.getBic());
        assertNull(mandate.getBankName());
        assertFalse(mandate.isAcceptedTerms(), "acceptedTerms should default to false");
    }
}
