package entity.dto;

import entity.DocumentType;
import entity.User;

import java.time.LocalDate;

public class ClientDto {
    private Long id;
    private String clientId;
    private User user;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private DocumentType documentType;
    private String documentId;
    private String documentPrefix;
    private String documentSuffix;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getDocumentPrefix() {
        return documentPrefix;
    }

    public void setDocumentPrefix(String documentPrefix) {
        this.documentPrefix = documentPrefix;
    }

    public String getDocumentSuffix() {
        return documentSuffix;
    }

    public void setDocumentSuffix(String documentSuffix) {
        this.documentSuffix = documentSuffix;
    }
}
