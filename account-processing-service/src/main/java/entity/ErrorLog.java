package entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "error_log")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String microserviceName;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDate timestamp;

    public ErrorLog() {}

    public ErrorLog(String microserviceName, String type, String message, LocalDate timestamp) {
        this.microserviceName = microserviceName;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMicroserviceName() { return microserviceName; }
    public void setMicroserviceName(String microserviceName) { this.microserviceName = microserviceName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }
}
