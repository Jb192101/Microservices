package entity;

public enum PaymentStatus {
    PENDING("На рассмотрении"),
    PAID("Оплачено"),
    OVERDUE("Просрочено"),
    CANCELLED("Отменённый");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}