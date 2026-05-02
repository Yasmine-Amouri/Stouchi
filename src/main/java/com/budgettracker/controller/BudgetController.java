package com.budgettracker.controller;

import com.budgettracker.entity.MonthlyBudget;
import com.budgettracker.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgetStatus(month, year));
    }

    @GetMapping
    public ResponseEntity<?> getBudget(@RequestParam int month, @RequestParam int year) {
        return budgetService.getBudget(month, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> setOrUpdateBudget(@RequestBody BudgetRequest body) {
        if (body == null || body.getMonth() == null || body.getYear() == null || body.getBudgetLimit() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "month, year, and budgetLimit are required"));
        }
        if (body.getBudgetLimit() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "budgetLimit must be greater than 0"));
        }
        return ResponseEntity.ok(
                budgetService.setOrUpdateBudget(body.getMonth(), body.getYear(), body.getBudgetLimit())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    private static class BudgetRequest {
        private Integer month;
        private Integer year;
        private Double budgetLimit;

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Double getBudgetLimit() {
            return budgetLimit;
        }

        public void setBudgetLimit(Double budgetLimit) {
            this.budgetLimit = budgetLimit;
        }
    }
}
