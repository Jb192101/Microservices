package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "products")
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Название продукта обязательно!")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Ключ продукта обязателен!")
    private ProductKey key;

    @Column(name = "create_date", nullable = false)
    @NotNull(message = "Дата создания обязательна!")
    private LocalDate createDate;

    public Product() {}

    public Product(Long id, String name, ProductKey key, LocalDate createDate) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.createDate = createDate;
    }

    public String getProductId() {
        return key.toString() + id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductKey getKey() {
        return key;
    }

    public void setKey(ProductKey key) {
        this.key = key;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }
}