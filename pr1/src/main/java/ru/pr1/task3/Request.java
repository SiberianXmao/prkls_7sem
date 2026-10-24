package ru.pr1.task3;

import java.math.BigDecimal;

public record Request(String id, int quantity, BigDecimal unitPrice, int discountPercent) {
}
