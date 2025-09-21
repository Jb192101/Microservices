package entity;

public enum TransactionStatus {
    ALLOWED("Разрешено"),
    PROCESSING("Обрабатывается"),
    COMPLETE("Завершено"),
    BLOCKED("Заблокировано"),
    CANCELLED("Отменено");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
