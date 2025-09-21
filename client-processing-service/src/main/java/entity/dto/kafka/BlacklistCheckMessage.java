package entity.dto.kafka;

import entity.DocumentType;

import java.time.LocalDate;

public class BlacklistCheckMessage {
    private DocumentType documentType;
    private String documentId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    public BlacklistCheckMessage(DocumentType documentType, String documentId, String firstName, String lastName,
                                 LocalDate dateOfBirth) {
        this.documentType = documentType;
        this.documentId = documentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }
}