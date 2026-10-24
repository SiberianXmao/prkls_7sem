package ru.pr3.model;

public record ProcessingIssue(long supplierId, String product, String message) {
    public static ProcessingIssue from(SupplierProduct source, Throwable error) {
        String productName = source.product() == null || source.product().isBlank()
                ? "<не задан>"
                : source.product();
        return new ProcessingIssue(source.supplierId(), productName, error.getMessage());
    }
}
