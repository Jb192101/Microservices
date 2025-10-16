package kafka.dto;

import java.time.LocalDate;

public class ClientInfoDTO {
    private Long clientId;
    private String fullName;
    private String documentNumber;
    private LocalDate documentIssueDate;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public LocalDate getDocumentIssueDate() {
        return documentIssueDate;
    }

    public void setDocumentIssueDate(LocalDate documentIssueDate) {
        this.documentIssueDate = documentIssueDate;
    }
}
