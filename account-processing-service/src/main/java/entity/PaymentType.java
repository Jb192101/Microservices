package entity;

public enum PaymentType {
    TRANSFER("Перевод"),
    DEPOSIT("Депозит"),
    WITHDRAWAL("Вывод"),
    FEE("Комиссия"),
    INTEREST("Проценты"),
    LOAN_PAYMENT("Оплата по кредиту"),
    CARD_PAYMENT("Оплата картой"),
    REFUND("Возврат средств"),
    SALARY("Зарплата");

    private final String value;

    PaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
