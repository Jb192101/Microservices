package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "client_products")
@Builder
public class ClientProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Клиент не может быть пустым!")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Продукт не может быть пустым!")
    private Product product;

    @Column(name = "open_date", nullable = false)
    @NotNull(message = "Дата открытия не может быть пустой!")
    private LocalDate openDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Статус не может быть пустым!")
    private ClientProductStatus status;

    public ClientProduct() {}

    public ClientProduct(Long id, Client client, Product product, LocalDate openDate, LocalDate closeDate,
                         ClientProductStatus status) {
        this.id = id;
        this.client = client;
        this.product = product;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public ClientProductStatus getStatus() {
        return status;
    }

    public void setStatus(ClientProductStatus status) {
        this.status = status;
    }
}
