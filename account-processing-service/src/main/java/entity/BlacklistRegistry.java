package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name="black_list")
public class BlacklistRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="document_type", nullable = false)
    @NotNull(message = "Тип документа не может быть пустым!")
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name="document_id", nullable = false)
    private String documentId;

    @Column(name="blacklisted_at", nullable = false)
    private String blacklistedAt;

    @Column(name="reason", nullable = false)
    private String reason;

    @Column(name="blacklist_expiration_date", nullable = false)
    private LocalDate blackListExpirationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getBlackListExpirationDate() {
        return blackListExpirationDate;
    }

    public void setBlackListExpirationDate(LocalDate blackListExpirationDate) {
        this.blackListExpirationDate = blackListExpirationDate;
    }

    public String getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(String blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }
}
