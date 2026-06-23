package com.pathiful.payment;

import com.pathiful.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sepa_mandates")
public class SepaMandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "mandate_reference", nullable = false, unique = true, length = 50)
    private String mandateReference;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    @Column(name = "iban_masked", nullable = false, length = 50)
    private String ibanMasked;

    @Column(name = "iban_encrypted", nullable = false, length = 255)
    private String ibanEncrypted;

    @Column(length = 20)
    private String bic;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "accepted_terms", nullable = false)
    private boolean acceptedTerms;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SepaMandateStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SepaMandateStatus.DRAFT;
        }
    }

    public SepaMandate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMandateReference() { return mandateReference; }
    public void setMandateReference(String mandateReference) { this.mandateReference = mandateReference; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public String getIbanMasked() { return ibanMasked; }
    public void setIbanMasked(String ibanMasked) { this.ibanMasked = ibanMasked; }
    public String getIbanEncrypted() { return ibanEncrypted; }
    public void setIbanEncrypted(String ibanEncrypted) { this.ibanEncrypted = ibanEncrypted; }
    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public boolean isAcceptedTerms() { return acceptedTerms; }
    public void setAcceptedTerms(boolean acceptedTerms) { this.acceptedTerms = acceptedTerms; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public SepaMandateStatus getStatus() { return status; }
    public void setStatus(SepaMandateStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum SepaMandateStatus {
        DRAFT,
        ACTIVE,
        REVOKED
    }
}
