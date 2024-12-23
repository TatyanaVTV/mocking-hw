package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.ProductNotFoundException;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.model.Delivery;
import ru.productstar.mockito.model.Order;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.InitRepository;
import ru.productstar.mockito.repository.OrderRepository;
import ru.productstar.mockito.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    /**
     * Покрыть тестами методы create и addProduct.
     * Можно использовать вызовы реальных методов.
     * <p>
     * Должны быть проверены следующие сценарии:
     * - создание ордера для существующего и нового клиента
     * - добавление существующего и несуществующего товара
     * - добавление товара в достаточном и не достаточном количестве
     * - заказ товара с быстрой доставкой
     * <p>
     * Проверки:
     * - общая сумма заказа соответствует ожидаемой
     * - корректная работа для несуществующего товара
     * - порядок и количество вызовов зависимых сервисов
     * - факт выбрасывания ProductNotFoundException
     */

    @Spy
    CustomerService customerService = new CustomerService(InitRepository.getInstance().getCustomerRepository());

    @Spy
    WarehouseService warehouseService = new WarehouseService(InitRepository.getInstance().getWarehouseRepository());

    @Spy
    OrderRepository orderRepository = InitRepository.getInstance().getOrderRepository();

    @Spy
    ProductRepository productRepository = InitRepository.getInstance().getProductRepository();

    private static final String EXISTING_CUSTOMER_NAME = "Ivan";
    private static final String NOT_EXISTING_CUSTOMER_NAME = "Matvey";

    private static final String EXISTING_PRODUCT = "phone";
    private static final int EXISTING_PRODUCT_MAX_COUNT = 5;
    private static final int EXISTING_PRODUCT_USED_PRICE = 400;
    private static final String NOT_EXISTING_PRODUCT = "mouse";

    @Test
    public void test_Create_ForExistingCustomer() {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(EXISTING_CUSTOMER_NAME);
        assertNotNull(order);

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_Create_ForNotExistingCustomer() {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(NOT_EXISTING_CUSTOMER_NAME);
        assertNotNull(order);

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(NOT_EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_AddProduct_WithExistingProduct_EnoughCount_WithFullPriceCheck() throws ProductNotFoundException {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(EXISTING_CUSTOMER_NAME);
        assertNotNull(order);

        orderService.addProduct(order, EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT, false);

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verify(warehouseService).findWarehouse(EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT);
        inOrder.verify(productRepository).getByName(EXISTING_PRODUCT);
        inOrder.verify(warehouseService).getStock(isA(Warehouse.class), eq(EXISTING_PRODUCT));
        inOrder.verify(orderRepository).addDelivery(eq(order.getId()), isA(Delivery.class));
        inOrder.verifyNoMoreInteractions();

        assertEquals(EXISTING_PRODUCT_USED_PRICE * EXISTING_PRODUCT_MAX_COUNT,
                order.getDeliveries().stream().mapToInt(delivery -> delivery.getCount() * delivery.getPrice()).sum());
    }

    @Test
    public void test_AddProduct_WithExistingProduct_FastDelivery() throws ProductNotFoundException {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(EXISTING_CUSTOMER_NAME);
        assertNotNull(order);

        orderService.addProduct(order, EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT, true);

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verify(warehouseService).findClosestWarehouse(EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT);
        inOrder.verify(productRepository).getByName(EXISTING_PRODUCT);
        inOrder.verify(warehouseService).getStock(isA(Warehouse.class), eq(EXISTING_PRODUCT));
        inOrder.verify(orderRepository).addDelivery(eq(order.getId()), isA(Delivery.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_AddProduct_WithExistingProduct_NotEnoughCount_ThrowsProductNotFoundException() {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(EXISTING_CUSTOMER_NAME);
        assertNotNull(order);
        assertThrows(ProductNotFoundException.class,
                () -> orderService.addProduct(order, EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT + 1, false));

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verify(warehouseService).findWarehouse(EXISTING_PRODUCT, EXISTING_PRODUCT_MAX_COUNT + 1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_AddProduct_WithNotExistingProduct_ThrowsProductNotFoundException() {
        OrderService orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);

        Order order = orderService.create(EXISTING_CUSTOMER_NAME);
        assertNotNull(order);
        assertThrows(ProductNotFoundException.class,
                () -> orderService.addProduct(order, NOT_EXISTING_PRODUCT, 1, false));

        InOrder inOrder = inOrder(customerService, warehouseService, orderRepository, productRepository);
        inOrder.verify(customerService).getOrCreate(EXISTING_CUSTOMER_NAME);
        inOrder.verify(orderRepository).create(isA(Customer.class));
        inOrder.verify(warehouseService).findWarehouse(NOT_EXISTING_PRODUCT, 1);
        inOrder.verifyNoMoreInteractions();
    }
}