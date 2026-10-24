package ru.pr8.order.service;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order " + id + " not found");
    }
}
