package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.InitRepository;
import ru.productstar.mockito.repository.ProductRepository;
import ru.productstar.mockito.repository.WarehouseRepository;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    /**
     * Покрыть тестами методы findWarehouse и findClosestWarehouse.
     * Вызывать реальные методы зависимых сервисов и репозиториев нельзя.
     * Поиск должен осуществляться как минимум на трех складах.
     *
     * Должны быть проверены следующие сценарии:
     * - поиск несуществующего товара
     * - поиск существующего товара с достаточным количеством
     * - поиск существующего товара с недостаточным количеством
     *
     * Проверки:
     * - товар находится на нужном складе, учитывается количество и расстояние до него
     * - корректная работа для несуществующего товара
     * - порядок и количество вызовов зависимых сервисов
     */

    private static final String EXISTING_IN_ALL_WH_PRODUCT = "RAM";
    private static final String EXISTING_IN_SINGE_WH_PRODUCT = "Micro";
    private static final String NOT_EXISTING_PRODUCT = "mouse";

    @Mock
    ProductRepository productRepo = InitRepository.getInstance().getProductRepository();
    @Mock
    WarehouseRepository warehouseRepo = InitRepository.getInstance().getWarehouseRepository();

    @BeforeEach
    void setup() {
        Product ram = new Product(EXISTING_IN_ALL_WH_PRODUCT);
        Product micro = new Product(EXISTING_IN_SINGE_WH_PRODUCT);
        lenient().when(productRepo.getByName(ram.getName())).thenReturn(ram);
        lenient().when(productRepo.getByName(micro.getName())).thenReturn(micro);
        lenient().when(productRepo.getByName(NOT_EXISTING_PRODUCT)).thenReturn(null);

        Warehouse wh0 = new Warehouse("MockWarehouse0", 30);
        wh0.addStock(new Stock(productRepo.getByName(EXISTING_IN_ALL_WH_PRODUCT), 200, 5));
        wh0.addStock(new Stock(productRepo.getByName(EXISTING_IN_SINGE_WH_PRODUCT), 75, 10));

        Warehouse wh1 = new Warehouse("MockWarehouse1", 20);
        wh1.addStock(new Stock(productRepo.getByName(EXISTING_IN_ALL_WH_PRODUCT), 180, 2));

        Warehouse wh2 = new Warehouse("MockWarehouse2", 50);
        wh2.addStock(new Stock(productRepo.getByName(EXISTING_IN_ALL_WH_PRODUCT), 250, 3));

        lenient().when(warehouseRepo.getById(0)).thenReturn(wh0);
        lenient().when(warehouseRepo.all()).thenReturn(Arrays.asList(wh0, wh1, wh2));
    }

    @Test
    public void test_findWarehouse_NotExistingProduct() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findWarehouse(NOT_EXISTING_PRODUCT, 1);

        assertNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_findWarehouse_ExistingProduct_EnoughCount() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findWarehouse(EXISTING_IN_ALL_WH_PRODUCT, 5);

        assertNotNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_findWarehouse_ExistingProduct_NotEnoughCount() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findWarehouse(EXISTING_IN_ALL_WH_PRODUCT, 10);

        assertNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_findClosestWarehouse_NotExistingProduct() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findClosestWarehouse(NOT_EXISTING_PRODUCT, 1);

        assertNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_findClosestWarehouse_ExistingProduct_AvailableInMoreThanOnWarehouse() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findClosestWarehouse(EXISTING_IN_ALL_WH_PRODUCT, 1);

        assertNotNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_findClosestWarehouse_ExistingProduct_AvailableInOnlyOneWarehouse() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh = warehouseService.findClosestWarehouse(EXISTING_IN_SINGE_WH_PRODUCT, 1);

        assertNotNull(wh);

        InOrder inOrder = inOrder(warehouseRepo, productRepo);
        inOrder.verify(warehouseRepo).all();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void test_getStock_ExistingProduct() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh0 = warehouseRepo.getById(0);
        Stock stock = warehouseService.getStock(wh0, EXISTING_IN_ALL_WH_PRODUCT);

        assertNotNull(stock);
    }

    @Test
    public void test_getStock_NotExistingProduct() {
        WarehouseService warehouseService = new WarehouseService(warehouseRepo);
        Warehouse wh0 = warehouseRepo.getById(0);
        Stock stock = warehouseService.getStock(wh0, NOT_EXISTING_PRODUCT);

        assertNull(stock);
    }
}
