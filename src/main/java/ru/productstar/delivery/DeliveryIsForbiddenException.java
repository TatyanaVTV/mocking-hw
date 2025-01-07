package ru.productstar.delivery;

public class DeliveryIsForbiddenException extends IllegalArgumentException {
    public DeliveryIsForbiddenException(String message) {
        super(String.format("Доставка данного груза по указанным параметрам невозможна! %s", message));
    }
}