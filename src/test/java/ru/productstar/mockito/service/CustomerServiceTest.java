package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.repository.CustomerRepository;
import ru.productstar.mockito.repository.InitRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    /**
     * Тест 1 - Получение покупателя "Ivan"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * <p>
     * Тест 2 - Получение покупателя "Oleg"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * - в метод getOrCreate была передана строка "Oleg"
     */

    @Spy
    CustomerRepository customerRepo = InitRepository.getInstance().getCustomerRepository();

    @Test
    public void mockExistingCustomerTest() {
        CustomerService customerService = new CustomerService(customerRepo);
        Customer customer = customerService.getOrCreate("Ivan");

        assertNotNull(customer);

        InOrder inOrder = inOrder(customerRepo);
        inOrder.verify(customerRepo, times(1)).getByName("Ivan");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void mockNotExistingCustomerTest() {
        CustomerService customerService = new CustomerService(customerRepo);
        Customer customer = customerService.getOrCreate("Oleg");

        assertNotNull(customer);

        InOrder inOrder = inOrder(customerRepo);
        inOrder.verify(customerRepo, times(1)).getByName("Oleg");
        inOrder.verify(customerRepo, times(1)).add(isA(Customer.class));
        inOrder.verifyNoMoreInteractions();
    }
}