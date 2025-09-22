package entity;

public enum CardStatus {
    ACTIVE("Активная"),
    BLOCKED("Заблокированная"),
    EXPIRED("Истекший срок годности");

    private final String value;

    CardStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
