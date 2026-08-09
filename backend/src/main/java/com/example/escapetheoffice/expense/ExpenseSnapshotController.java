package com.example.escapetheoffice.expense;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.expense.dto.ExpenseSnapshotCreateRequest;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseSnapshotController {

    private final ExpenseSnapshotService expenseSnapshotService;

    public ExpenseSnapshotController(ExpenseSnapshotService expenseSnapshotService) {
        this.expenseSnapshotService = expenseSnapshotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseSnapshotResponse create(@Valid @RequestBody ExpenseSnapshotCreateRequest request) {
        return expenseSnapshotService.create(request);
    }

    @GetMapping
    public List<ExpenseSnapshotResponse> findAll() {
        return expenseSnapshotService.findAll();
    }

    @GetMapping("/latest")
    public ExpenseSnapshotResponse findLatest() {
        return expenseSnapshotService.findLatest();
    }
}
