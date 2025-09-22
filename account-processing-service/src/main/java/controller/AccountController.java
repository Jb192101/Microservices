package controller;

import entity.Account;
import entity.AccountStatus;
import service.AccountService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Account>> getAccountsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(accountService.getAccountsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/active")
    public ResponseEntity<List<Account>> getActiveAccountsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(accountService.getActiveAccountsByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<Account> updateAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam boolean isCredit) {
        return ResponseEntity.ok(accountService.updateAccountBalance(id, amount, isCredit));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Account> updateAccountStatus(
            @PathVariable Long id,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(id, status));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        accountService.closeAccount(id);
        return ResponseEntity.ok().build();
    }
}
