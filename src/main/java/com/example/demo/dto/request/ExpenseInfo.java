package com.example.demo.dto.request;

public record ExpenseInfo(

        String category,
        String itemName,
        Double amount
) {
}
