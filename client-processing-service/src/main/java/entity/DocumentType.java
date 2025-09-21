package entity;

public enum DocumentType {
    PASSPORT("Паспорт"),
    INT_PASSPORT("Внутренний паспорт"),
    BIRTH_CERT("Свидетельство о рождении");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
