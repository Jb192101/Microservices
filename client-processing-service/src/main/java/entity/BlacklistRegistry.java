package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name="blacklist_registry")
public class BlacklistRegistry {
    private DocumentType documentType;
    private String documentId;
    private String blacklistedAt;
    private String reason;
    private LocalDate blacklistExpirationDate;

    public BlacklistRegistry(DocumentType documentType, String documentId, String blacklistedAt,
                             String reason, LocalDate blacklistExpirationDate) {
        this.documentType = documentType;
        this.documentId = documentId;
        this.blacklistedAt = blacklistedAt;
        this.reason = reason;
        this.blacklistExpirationDate = blacklistExpirationDate;
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

    public String getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(String blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getBlacklistExpirationDate() {
        return blacklistExpirationDate;
    }

    public void setBlacklistExpirationDate(LocalDate blacklistExpirationDate) {
        this.blacklistExpirationDate = blacklistExpirationDate;
    }
}
