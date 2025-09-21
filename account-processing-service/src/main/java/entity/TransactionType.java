package entity;

public enum TransactionType {
    DEPOSIT("Депозит"),
    WITHDRAWAL("Вывод"),
    TRANSFER_IN("Перевод на счёт"),
    TRANSFER_OUT("Перевод со счёта"),
    PAYMENT("Оплата"),
    FEE("Комиссия"),
    INTEREST("Проценты"),
    REFUND("Возврат средств");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }
}
