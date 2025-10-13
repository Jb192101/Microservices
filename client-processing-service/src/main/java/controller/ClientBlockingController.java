package controller;

import com.t1.starter.pack.aop.annotations.HttpIncomeRequestLog;
import service.ClientBlockingService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/client-blocking")
@PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')")
public class ClientBlockingController {
    @Autowired
    private ClientBlockingService clientBlockingService;

    @PostMapping("/block-by-client-id")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> blockClientByClientId(@RequestBody BlockRequest request) {
        clientBlockingService.blockClientByClientId(request.getClientId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block-by-user-id")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> blockClientByUserId(@RequestBody BlockRequest request) {
        clientBlockingService.blockClientByUserId(request.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock-by-client-id")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> unblockClientByClientId(@RequestBody BlockRequest request) {
        clientBlockingService.unblockClientByClientId(request.getClientId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock-by-user-id")
    @HttpIncomeRequestLog
    public ResponseEntity<Void> unblockClientByUserId(@RequestBody BlockRequest request) {
        clientBlockingService.unblockClientByUserId(request.getUserId());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class BlockRequest {
        private String clientId;
        private Long userId;
    }
}
