package entity.dto;

import entity.ProductKey;

public class ProductDto {
    private Long id;
    private String name;
    private ProductKey key;
    private String eventType;

    public Long getProductId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductKey getProductKey() {
        return key;
    }

    public void setKey(ProductKey key) {
        this.key = key;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
