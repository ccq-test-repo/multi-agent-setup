package com.pathiful.payment;

import com.pathiful.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service für SEPA-Lastschriftmandate.
 *
 * Keine echte Bankkommunikation — nur Simulation/Prototyp.
 * IBAN wird maskiert gespeichert + NIEMALS in Logs ausgegeben.
 */
@Service
public class SepaMandateService {

    private static final Logger log = LoggerFactory.getLogger(SepaMandateService.class);

    private final SepaMandateRepository repository;

    public SepaMandateService(SepaMandateRepository repository) {
        this.repository = repository;
    }

    /**
     * Erstellt ein neues SEPA-Mandat.
     */
    @Transactional
    public SepaMandateResponse createMandate(SepaMandateRequest request, User user) {
        if (repository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("Für diesen Benutzer existiert bereits ein SEPA-Mandat.");
        }

        String cleanedIban = request.getIban().replaceAll("\\s+", "").toUpperCase();

        SepaMandate mandate = new SepaMandate();
        mandate.setUser(user);
        mandate.setMandateReference(generateReference(user));
        mandate.setAccountHolderName(request.getAccountHolder().trim());
        mandate.setIbanMasked(maskIban(cleanedIban));
        mandate.setIbanEncrypted("encrypted:" + cleanedIban);
        mandate.setBic(request.getBic() != null ? request.getBic().trim().toUpperCase() : null);
        mandate.setBankName(request.getBankName() != null ? request.getBankName().trim() : null);
        mandate.setAcceptedTerms(true);
        mandate.setAcceptedAt(LocalDateTime.now());
        mandate.setStatus(SepaMandate.SepaMandateStatus.ACTIVE);

        SepaMandate saved = repository.save(mandate);

        log.info("SEPA mandate created: userId={}, ref={}, status=ACTIVE",
                user.getId(), saved.getMandateReference());

        return SepaMandateResponse.fromEntity(saved);
    }

    /**
     * Ruft ein bestehendes Mandat ab (IBAN maskiert).
     */
    @Transactional(readOnly = true)
    public Optional<SepaMandateResponse> getMandate(Long userId) {
        return repository.findByUserId(userId)
                .map(SepaMandateResponse::fromEntity);
    }

    /**
     * Widerruft ein Mandat (Status -> REVOKED).
     */
    @Transactional
    public SepaMandateResponse revokeMandate(Long userId) {
        SepaMandate mandate = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kein SEPA-Mandat für diesen Benutzer gefunden."));

        if (mandate.getStatus() == SepaMandate.SepaMandateStatus.REVOKED) {
            throw new IllegalArgumentException("SEPA-Mandat wurde bereits widerrufen.");
        }

        mandate.setStatus(SepaMandate.SepaMandateStatus.REVOKED);
        SepaMandate saved = repository.save(mandate);

        log.info("SEPA mandate revoked: userId={}, ref={}", userId, saved.getMandateReference());

        return SepaMandateResponse.fromEntity(saved);
    }

    // -----------------------------------------------------------------------
    // Hilfsfunktionen
    // -----------------------------------------------------------------------

    private String generateReference(User user) {
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "PATHIFUL-" + shortUuid;
    }

    /**
     * Maskiert eine IBAN für UI-Anzeige.
     * Zeigt erste 4 und letzte 4 Zeichen; der Rest wird mit verdeckt.
     */
    public static String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        String prefix = iban.substring(0, 4);
        String suffix = iban.substring(iban.length() - 4);
        return prefix + " **** **** " + suffix;
    }
}
