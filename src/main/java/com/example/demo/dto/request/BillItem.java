package com.example.demo.dto.request;

public record BillItem(String name,
                       Integer quantity,
                       Double price,
                       Double subTotal
                       ) {
}
