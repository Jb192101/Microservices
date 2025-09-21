package entity.dto.kafka.enums;

public enum ActionEnum {
    CREATE("Создать"),
    UPDATE("Обновить"),
    DELETE("Удалить");

    private final String value;

    ActionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
