package entity;

public enum ClientProductStatus {
    ACTIVE("Активный"),
    CLOSED("Закрытый"),
    BLOCKED("Заблокированный"),
    ARRESTED("Арестованный");

    private final String value;

    ClientProductStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
