package util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ClientIdGenerator {
    private static final String REGION = "77";
    private static final String DIVISION = "01";
    private final AtomicLong counter = new AtomicLong(1);

    public String generateClientId() {
        long sequence = counter.getAndIncrement();
        return String.format("%s%s%08d", REGION, DIVISION, sequence);
    }
}