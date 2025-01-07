package ru.productstar.delivery;

import ru.productstar.delivery.dicts.CargoFragility;
import ru.productstar.delivery.dicts.CargoSize;
import ru.productstar.delivery.dicts.DeliveryServiceWorkload;
import ru.productstar.delivery.dicts.Distance;

import static ru.productstar.delivery.dicts.CargoFragility.FRAGILE;
import static ru.productstar.delivery.dicts.DeliveryCostCoefficients.*;
import static ru.productstar.delivery.dicts.DeliveryPrices.*;
import static ru.productstar.delivery.dicts.Distance.OVER_30_KM;

public class DeliveryCost {
    public static double calculateDeliveryCost(Distance distance,
                                        CargoSize cargoSize,
                                        CargoFragility cargoFragility,
                                        DeliveryServiceWorkload deliveryServiceWorkload) {

        if (distance == OVER_30_KM && cargoFragility == FRAGILE) {
            throw new DeliveryIsForbiddenException("Хрупкие грузы нельзя возить на расстояние более 30 км.");
        }

        double deliveryCost = 0.00;
        deliveryCost += calculatePriceForDistance(distance);
        deliveryCost += calculatePriceForCargoSize(cargoSize);
        deliveryCost += calculatePriceForCargoFragility(cargoFragility);
        deliveryCost *= getCoefficientForDeliveryServiceWorkload(deliveryServiceWorkload);

        return Math.max(deliveryCost, MIN_DELIVERY_PRICE);
    }

    protected static double calculatePriceForDistance(Distance distance) {
        return switch(distance) {
            case OVER_30_KM -> OVER_30_KM_PRICE;
            case LESS_30_KM -> LESS_30_KM_PRICE;
            case LESS_10_KM -> LESS_10_KM_PRICE;
            case LESS_2_KM -> LESS_2_KM_PRICE;
        };
    }

    protected static double calculatePriceForCargoSize(CargoSize cargoSize) {
        return switch(cargoSize) {
            case LARGE_CARGO -> LARGE_CARGO_PRICE;
            case SMALL_CARGO -> SMALL_CARGO_PRICE;
        };
    }

    protected static double calculatePriceForCargoFragility(CargoFragility cargoFragility) {
        return cargoFragility == FRAGILE ? FRAGILE_PRICE : NOT_FRAGILE_PRICE;
    }

    protected static double getCoefficientForDeliveryServiceWorkload(DeliveryServiceWorkload deliveryServiceWorkload) {
        return switch(deliveryServiceWorkload) {
            case VERY_HIGH_WORKLOAD -> VERY_HIGH_WORKLOAD_COEFFICIENT;
            case HIGH_WORKLOAD -> HIGH_WORKLOAD_COEFFICIENT;
            case INCREASED_WORKLOAD -> INCREASED_WORKLOAD_COEFFICIENT;
            default -> DEFAULT_COEFFICIENT;
        };
    }
}