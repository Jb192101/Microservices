package kafka.dto;

import jakarta.validation.constraints.NotNull;

public class CreditDecisionDTO {
    @NotNull
    private Boolean approved;

    private String reason;

    @NotNull
    private Long clientId;

    private Long productRegistryId;

    public CreditDecisionDTO() {}

    public CreditDecisionDTO(Boolean approved, String reason, Long clientId) {
        this.approved = approved;
        this.reason = reason;
        this.clientId = clientId;
    }

    public CreditDecisionDTO(Boolean approved, String reason, Long clientId, Long productRegistryId) {
        this.approved = approved;
        this.reason = reason;
        this.clientId = clientId;
        this.productRegistryId = productRegistryId;
    }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getProductRegistryId() { return productRegistryId; }
    public void setProductRegistryId(Long productRegistryId) { this.productRegistryId = productRegistryId; }
}
