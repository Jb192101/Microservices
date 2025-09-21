package entity;

public enum AccountStatus {
    ACTIVE("Активный"),
    BLOCKED("Заблокированный"),
    CLOSED("Закрытый"),
    FROZEN("Замороженный"),
    OVERDRAWN("Перерасходованный");

    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
