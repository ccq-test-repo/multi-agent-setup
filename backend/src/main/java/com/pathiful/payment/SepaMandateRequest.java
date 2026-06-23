package com.pathiful.payment;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request-DTO für POST /api/payment/sepa-mandate
 */
public class SepaMandateRequest {

    @NotBlank(message = "Kontoinhaber ist erforderlich")
    @Size(max = 150, message = "Kontoinhaber maximal 150 Zeichen")
    private String accountHolder;

    @NotBlank(message = "IBAN ist erforderlich")
    @Pattern(regexp = "^[A-Za-z]{2}[0-9A-Za-z\\s]{10,32}$",
             message = "Ungültiges IBAN-Format")
    private String iban;

    private String bic;

    private String bankName;

    private boolean acceptedTerms;

    @AssertTrue(message = "Zustimmung zu den SEPA-Bedingungen ist erforderlich")
    public boolean isTermsAccepted() {
        return acceptedTerms;
    }

    // Kein weiteres isAcceptedTerms() — @AssertTrue nutzt isTermsAccepted()

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public boolean isAcceptedTerms() { return acceptedTerms; }
    public void setAcceptedTerms(boolean acceptedTerms) { this.acceptedTerms = acceptedTerms; }
}
