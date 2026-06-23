package com.pathiful.payment;

import com.pathiful.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepaMandateServiceTest {

    @Mock
    private SepaMandateRepository repository;

    private SepaMandateService service;

    private User user;

    @BeforeEach
    void setUp() {
        service = new SepaMandateService(repository);
        user = new User("test@example.com", "hash", User.Role.USER);
        user.setId(42L);
    }

    // -----------------------------------------------------------------------
    // createMandate – Success
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateMandateSuccessfully() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("DE89 3704 0044 0532 0130 00");
        request.setBic("COBADEFFXXX");
        request.setBankName("Commerzbank");
        request.setAcceptedTerms(true);

        when(repository.existsByUserId(42L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            SepaMandate m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        SepaMandateResponse response = service.createMandate(request, user);

        assertThat(response).isNotNull();
        assertThat(response.getMandateReference()).startsWith("PATHIFUL-");
        assertThat(response.getAccountHolderName()).isEqualTo("Max Mustermann");
        assertThat(response.getIbanMasked()).contains("DE89");
        assertThat(response.getIbanMasked()).contains("****");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getBic()).isEqualTo("COBADEFFXXX");

        verify(repository).save(argThat(m ->
            m.getStatus() == SepaMandate.SepaMandateStatus.ACTIVE &&
            m.getIbanEncrypted().startsWith("encrypted:") &&
            m.getIbanMasked().startsWith("DE89") &&
            m.isAcceptedTerms()
        ));
    }

    // -----------------------------------------------------------------------
    // createMandate – Duplicate
    // -----------------------------------------------------------------------

    @Test
    void shouldRejectDuplicateMandate() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max");
        request.setIban("DE89370400440532013000");
        request.setAcceptedTerms(true);

        when(repository.existsByUserId(42L)).thenReturn(true);

        assertThatThrownBy(() -> service.createMandate(request, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bereits ein SEPA-Mandat");

        verify(repository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // createMandate – IBAN cleanup
    // -----------------------------------------------------------------------

    @Test
    void shouldCleanIbanSpacesAndUppercase() {
        SepaMandateRequest request = new SepaMandateRequest();
        request.setAccountHolder("Max Mustermann");
        request.setIban("  de89 3704 0044 0532 0130 00  ");
        request.setAcceptedTerms(true);

        when(repository.existsByUserId(42L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            SepaMandate m = inv.getArgument(0);
            m.setId(2L);
            return m;
        });

        SepaMandateResponse response = service.createMandate(request, user);

        assertThat(response.getIbanMasked()).startsWith("DE89");
        assertThat(response.getIbanMasked()).endsWith("3000");
    }

    // -----------------------------------------------------------------------
    // getMandate
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnMandateForUser() {
        SepaMandate mandate = new SepaMandate();
        mandate.setId(1L);
        mandate.setUser(user);
        mandate.setMandateReference("PATHIFUL-TEST1234");
        mandate.setAccountHolderName("Max");
        mandate.setIbanMasked("DE89 **** **** 1300");
        mandate.setIbanEncrypted("encrypted:DE89370400440532013000");
        mandate.setAcceptedTerms(true);
        mandate.setAcceptedAt(java.time.LocalDateTime.now());
        mandate.setStatus(SepaMandate.SepaMandateStatus.ACTIVE);

        when(repository.findByUserId(42L)).thenReturn(Optional.of(mandate));

        Optional<SepaMandateResponse> result = service.getMandate(42L);

        assertThat(result).isPresent();
        assertThat(result.get().getIbanMasked()).isEqualTo("DE89 **** **** 1300");
        assertThat(result.get().getMandateReference()).isEqualTo("PATHIFUL-TEST1234");
    }

    @Test
    void shouldReturnEmptyForNoMandate() {
        when(repository.findByUserId(42L)).thenReturn(Optional.empty());

        Optional<SepaMandateResponse> result = service.getMandate(42L);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // revokeMandate
    // -----------------------------------------------------------------------

    @Test
    void shouldRevokeActiveMandate() {
        SepaMandate mandate = new SepaMandate();
        mandate.setId(1L);
        mandate.setUser(user);
        mandate.setMandateReference("PATHIFUL-REVOKEME");
        mandate.setIbanMasked("DE89 **** **** 1300");
        mandate.setIbanEncrypted("encrypted:...");
        mandate.setAcceptedTerms(true);
        mandate.setStatus(SepaMandate.SepaMandateStatus.ACTIVE);
        mandate.setAcceptedAt(java.time.LocalDateTime.now());

        when(repository.findByUserId(42L)).thenReturn(Optional.of(mandate));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SepaMandateResponse response = service.revokeMandate(42L);

        assertThat(response.getStatus()).isEqualTo("REVOKED");
    }

    @Test
    void shouldRejectRevokeForNonExistentMandate() {
        when(repository.findByUserId(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeMandate(42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kein SEPA-Mandat");
    }

    @Test
    void shouldRejectRevokeForAlreadyRevokedMandate() {
        SepaMandate mandate = new SepaMandate();
        mandate.setId(1L);
        mandate.setUser(user);
        mandate.setIbanMasked("DE89 **** **** 1300");
        mandate.setIbanEncrypted("encrypted:...");
        mandate.setStatus(SepaMandate.SepaMandateStatus.REVOKED);

        when(repository.findByUserId(42L)).thenReturn(Optional.of(mandate));

        assertThatThrownBy(() -> service.revokeMandate(42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bereits widerrufen");
    }

    // -----------------------------------------------------------------------
    // IBAN-Maskierung
    // -----------------------------------------------------------------------

    @Test
    void shouldMaskGermanIbanCorrectly() {
        String masked = SepaMandateService.maskIban("DE89370400440532013000");
        assertThat(masked).isEqualTo("DE89 **** **** 3000");
    }

    @Test
    void shouldMaskFrenchIbanCorrectly() {
        String masked = SepaMandateService.maskIban("FR7630006000011234567890189");
        assertThat(masked).startsWith("FR76");
        assertThat(masked).endsWith("0189");
        assertThat(masked).contains("****");
    }

    @Test
    void shouldReturnNullForNullIban() {
        assertThat(SepaMandateService.maskIban(null)).isNull();
    }

    @Test
    void shouldReturnShortIbanUnchanged() {
        assertThat(SepaMandateService.maskIban("DE12")).isEqualTo("DE12");
    }
}
