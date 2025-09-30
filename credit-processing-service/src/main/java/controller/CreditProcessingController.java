package controller;

import aop.annotations.HttpIncomeRequestLog;
import aop.annotations.HttpOutcomeRequestLog;
import kafka.dto.CreditDecisionDTO;
import kafka.dto.CreditProductRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CreditDecisionService;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditProcessingController {
    @Autowired
    private final CreditDecisionService creditDecisionService;

    @PostMapping("/decision")
    @HttpIncomeRequestLog
    public ResponseEntity<CreditDecisionDTO> makeCreditDecision(
            @RequestBody CreditProductRequestDTO request) {
        CreditDecisionDTO decision = creditDecisionService.makeCreditDecision(request);
        return ResponseEntity.ok(decision);
    }

    @GetMapping("/health")
    @HttpOutcomeRequestLog
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("MS-3 Credit Processing Service is running");
    }
}