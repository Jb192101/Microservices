package service;

import entity.ProductKey;
import entity.ClientProductStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.ClientProductRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProductMetricsService {
    @Autowired
    private ClientProductRepository clientProductRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Map<ProductKey, AtomicInteger> productCounters = new HashMap<>();
    private final AtomicInteger totalActiveProducts = new AtomicInteger(0);

    @PostConstruct
    public void initMetrics() {
        Arrays.stream(ProductKey.values()).forEach(productKey -> {
            AtomicInteger counter = new AtomicInteger(0);
            productCounters.put(productKey, counter);

            Gauge.builder("product.count.by.type", counter, AtomicInteger::doubleValue)
                    .tag("product_type", productKey.name())
                    .description("Количество активных продуктов по типам")
                    .register(meterRegistry);
        });

        Gauge.builder("product.total.active", totalActiveProducts, AtomicInteger::doubleValue)
                .description("Общее количество активных продуктов")
                .register(meterRegistry);

        Counter.builder("product.assignment.total")
                .description("Общее количество назначений продуктов клиентам")
                .register(meterRegistry);

        updateAllMetrics();
    }

    public void updateProductMetrics() {
        productCounters.forEach((productKey, counter) -> {
            try {
                long count = clientProductRepository.countActiveProductsByProductKey(productKey);
                counter.set((int) count);
            } catch (Exception e) {
                counter.set(0);
            }
        });

        try {
            long totalCount = clientProductRepository.countByStatus(ClientProductStatus.ACTIVE);
            totalActiveProducts.set((int) totalCount);
        } catch (Exception e) {
            totalActiveProducts.set(0);
        }
    }

    public void incrementProductAssignment() {
        meterRegistry.counter("product.assignment.total").increment();
    }

    private void updateAllMetrics() {
        updateProductMetrics();
    }
}