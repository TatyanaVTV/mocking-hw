package ru.productstar.delivery;

import io.qameta.allure.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.delivery.dicts.CargoFragility;
import ru.productstar.delivery.dicts.CargoSize;
import ru.productstar.delivery.dicts.DeliveryServiceWorkload;
import ru.productstar.delivery.dicts.Distance;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.productstar.delivery.dicts.Distance.*;
import static ru.productstar.delivery.dicts.CargoFragility.*;
import static ru.productstar.delivery.dicts.CargoSize.*;
import static ru.productstar.delivery.dicts.DeliveryServiceWorkload.*;
import static ru.productstar.delivery.dicts.DeliveryPrices.*;
import static ru.productstar.delivery.dicts.DeliveryCostCoefficients.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryCostTests {
    @ParameterizedTest
    @MethodSource("provideArgumentsForDistancePriceTest")
    @DisplayName("Стоимость доставки в зависимости от расстояния.")
    @Description("Данный тест проверяет корректность суммы, на которую будет увеличиваться общая стоимость доставки, в зависимости от указанного расстояния до пункта назначения.")
    @Tag("distance")
    public void test_calculatePriceForDistance_ShouldReturnValidCost(Distance distance, double expectedPrice) {
        assertEquals(expectedPrice, DeliveryCost.calculatePriceForDistance(distance));
    }

    private static Stream<Arguments> provideArgumentsForDistancePriceTest() {
        return Stream.of(
                Arguments.of(OVER_30_KM, OVER_30_KM_PRICE),
                Arguments.of(LESS_30_KM, LESS_30_KM_PRICE),
                Arguments.of(LESS_10_KM, LESS_10_KM_PRICE),
                Arguments.of(LESS_2_KM, LESS_2_KM_PRICE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForCargoSizePriceTest")
    @DisplayName("Стоимость доставки в зависимости от габаритов груза.")
    @Description("Данный тест проверяет корректность суммы, на которую будет увеличиваться общая стоимость доставки, в зависимости от габаритов груза.")
    @Tag("cargoSize")
    public void test_calculatePriceForCargoSize_ShouldReturnValidCost(CargoSize cargoSize, double expectedPrice) {
        assertEquals(expectedPrice, DeliveryCost.calculatePriceForCargoSize(cargoSize));
    }

    private static Stream<Arguments> provideArgumentsForCargoSizePriceTest() {
        return Stream.of(
                Arguments.of(LARGE_CARGO, LARGE_CARGO_PRICE),
                Arguments.of(SMALL_CARGO, SMALL_CARGO_PRICE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForCargoFragilityPriceTest")
    @DisplayName("Стоимость доставки в зависимости от хрупкости груза.")
    @Description("Данный тест проверяет корректность суммы, на которую будет увеличиваться общая стоимость доставки, в зависимости от хрупкости груза.")
    @Tag("fragility")
    public void test_calculatePriceForCargoFragility_ShouldReturnValidCost(CargoFragility cargoFragility, double expectedPrice) {
        assertEquals(expectedPrice, DeliveryCost.calculatePriceForCargoFragility(cargoFragility));
    }

    private static Stream<Arguments> provideArgumentsForCargoFragilityPriceTest() {
        return Stream.of(
                Arguments.of(FRAGILE, FRAGILE_PRICE),
                Arguments.of(NOT_FRAGILE, NOT_FRAGILE_PRICE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForDeliveryServiceWorkloadCoeffTest")
    @DisplayName("Коэффициент в зависимости от загруженности службы доставки.")
    @Description("Данный тест проверяет корректность коэффициента, на который будет умножаться общая стоимость доставки, в зависимости от загруженности службы доставки на данный момени.")
    @Tag("deliveryServiceWorkload")
    public void test_getCoefficientForDeliveryServiceWorkload_ShouldReturnValidCoeff(
            DeliveryServiceWorkload deliveryServiceWorkload,
            double expectedCoefficient
    ) {
        assertEquals(expectedCoefficient, DeliveryCost.getCoefficientForDeliveryServiceWorkload(deliveryServiceWorkload));
    }

    private static Stream<Arguments> provideArgumentsForDeliveryServiceWorkloadCoeffTest() {
        return Stream.of(
                Arguments.of(VERY_HIGH_WORKLOAD, VERY_HIGH_WORKLOAD_COEFFICIENT),
                Arguments.of(HIGH_WORKLOAD, HIGH_WORKLOAD_COEFFICIENT),
                Arguments.of(INCREASED_WORKLOAD, INCREASED_WORKLOAD_COEFFICIENT),
                Arguments.of(REGULAR_WORKLOAD, DEFAULT_COEFFICIENT),
                Arguments.of(LOW_WORKLOAD, DEFAULT_COEFFICIENT)
        );
    }

    @Test
    @DisplayName("Хрупкие грузы при дистанции более 30 км.")
    @Description("Данный тест проверяет наличие ошибки при попытке заказать доставку хрупкого груза на расстояние более 30 км.")
    @Tag("fragile")
    public void test_calculateDeliveryCost_FragileCargo_over30Km_ShouldThrow_DeliveryIsForbiddenException() {
        assertThrows(DeliveryIsForbiddenException.class,
                () -> DeliveryCost.calculateDeliveryCost(OVER_30_KM, SMALL_CARGO, FRAGILE, LOW_WORKLOAD));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForCalculateDeliveryCostTest")
    @DisplayName("Общая стоимость доставки.")
    @Description("Данный тест проверяет корректность расчета общей стоимости доставки с учетом расстояния до пункта назначения, габаритов и хрупкости груза, а также загруженности службы доставки на данный момент.")
    @Tag("deliveryCost")
    public void test_calculateDeliveryCost_ShouldReturnValidCost(Distance distance,
                                                                 CargoSize cargoSize,
                                                                 CargoFragility cargoFragility,
                                                                 DeliveryServiceWorkload deliveryServiceWorkload,
                                                                 double expectedPrice) {
        assertEquals(expectedPrice, DeliveryCost.calculateDeliveryCost(distance, cargoSize, cargoFragility, deliveryServiceWorkload));
    }

    private static Stream<Arguments> provideArgumentsForCalculateDeliveryCostTest() {
        return Stream.of(
                Arguments.of(LESS_2_KM, SMALL_CARGO, NOT_FRAGILE, LOW_WORKLOAD, MIN_DELIVERY_PRICE),
                Arguments.of(LESS_30_KM, LARGE_CARGO, FRAGILE, VERY_HIGH_WORKLOAD,
                        (LESS_30_KM_PRICE + LARGE_CARGO_PRICE + FRAGILE_PRICE) * VERY_HIGH_WORKLOAD_COEFFICIENT),
                Arguments.of(LESS_10_KM, LARGE_CARGO, NOT_FRAGILE, VERY_HIGH_WORKLOAD,
                        (LESS_10_KM_PRICE + LARGE_CARGO_PRICE + NOT_FRAGILE_PRICE) * VERY_HIGH_WORKLOAD_COEFFICIENT),
                Arguments.of(LESS_2_KM, SMALL_CARGO, FRAGILE, HIGH_WORKLOAD,
                        (LESS_2_KM_PRICE + SMALL_CARGO_PRICE + FRAGILE_PRICE) * HIGH_WORKLOAD_COEFFICIENT),
                Arguments.of(LESS_2_KM, SMALL_CARGO, FRAGILE, INCREASED_WORKLOAD,
                        (LESS_2_KM_PRICE + SMALL_CARGO_PRICE + FRAGILE_PRICE) * INCREASED_WORKLOAD_COEFFICIENT),
                Arguments.of(LESS_30_KM, LARGE_CARGO, NOT_FRAGILE, REGULAR_WORKLOAD,
                        (LESS_30_KM_PRICE + LARGE_CARGO_PRICE + NOT_FRAGILE_PRICE) * DEFAULT_COEFFICIENT),
                Arguments.of(LESS_30_KM, LARGE_CARGO, FRAGILE, LOW_WORKLOAD,
                        (LESS_30_KM_PRICE + LARGE_CARGO_PRICE + FRAGILE_PRICE) * DEFAULT_COEFFICIENT),
                Arguments.of(OVER_30_KM, LARGE_CARGO, NOT_FRAGILE, LOW_WORKLOAD,
                        (OVER_30_KM_PRICE + LARGE_CARGO_PRICE + NOT_FRAGILE_PRICE) * DEFAULT_COEFFICIENT)
        );
    }
}