package com.pathiful.payment;

import java.time.LocalDateTime;

/**
 * Response-DTO für SEPA-Mandat (IBAN immer maskiert).
 */
public class SepaMandateResponse {

    private Long id;
    private String mandateReference;
    private String accountHolderName;
    private String ibanMasked;
    private String bic;
    private String bankName;
    private String status;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;

    public SepaMandateResponse() {}

    public static SepaMandateResponse fromEntity(SepaMandate mandate) {
        SepaMandateResponse resp = new SepaMandateResponse();
        resp.setId(mandate.getId());
        resp.setMandateReference(mandate.getMandateReference());
        resp.setAccountHolderName(mandate.getAccountHolderName());
        resp.setIbanMasked(mandate.getIbanMasked());
        resp.setBic(mandate.getBic());
        resp.setBankName(mandate.getBankName());
        resp.setStatus(mandate.getStatus().name());
        resp.setAcceptedAt(mandate.getAcceptedAt());
        resp.setCreatedAt(mandate.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMandateReference() { return mandateReference; }
    public void setMandateReference(String mandateReference) { this.mandateReference = mandateReference; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public String getIbanMasked() { return ibanMasked; }
    public void setIbanMasked(String ibanMasked) { this.ibanMasked = ibanMasked; }
    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
