package com.budgettracker.controller;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getByMonth(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) TransactionType type) {
        if (type != null) {
            return transactionService.getByMonthYearAndType(month, year, type);
        }
        return transactionService.getByMonthAndYear(month, year);
    }

    @GetMapping("/expenses-by-category")
    public List<Object[]> getExpensesByCategory(@RequestParam int month, @RequestParam int year) {
        return transactionService.getExpensesByCategory(month, year);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.create(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.update(id, transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
