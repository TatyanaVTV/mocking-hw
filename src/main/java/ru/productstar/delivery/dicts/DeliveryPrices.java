package ru.productstar.delivery.dicts;

public abstract class DeliveryPrices {
    public static double MIN_DELIVERY_PRICE = 400.00;

    public static double OVER_30_KM_PRICE = 300.00;
    public static double LESS_30_KM_PRICE = 200.00;
    public static double LESS_10_KM_PRICE = 100.00;
    public static double LESS_2_KM_PRICE = 50.00;

    public static double LARGE_CARGO_PRICE = 200.00;
    public static double SMALL_CARGO_PRICE = 100.00;

    public static double FRAGILE_PRICE = 300.00;
    public static double NOT_FRAGILE_PRICE = 0.00;
}