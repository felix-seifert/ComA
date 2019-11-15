/*
 * Copyright 2019 Felix Seifert <mail@felix-seifert.com> (https://felix-seifert.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.felixseifert.coma.backend.repos;

import com.felixseifert.coma.backend.model.enums.Currency;
import com.felixseifert.coma.backend.model.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customerExpected1;
    private Customer customerExpected2;
    private Customer customerExpected3;

    @Before
    public void setDatabase() {
        customerExpected1 = new Customer();
        customerExpected1.setCode("12345");
        customerExpected1.setName("Important Customer");
        customerExpected1.setDeliveryLocation("Kyoto, Japan");
        customerExpected1.setCurrency(Currency.JPY.toString());
        customerExpected2 = new Customer();
        customerExpected2.setCode("67890");
        customerExpected2.setName("Relevant Customer");
        customerExpected2.setDeliveryLocation("Nagano, Japan");
        customerExpected2.setCurrency(Currency.EUR.toString());
        customerExpected3 = new Customer();
        customerExpected3.setCode("22222");
        customerExpected3.setName("Reimbursement Customer");
        customerExpected3.setDeliveryLocation("Tokyo, Japan");
        customerExpected3.setCurrency(Currency.EUR.toString());

        testEntityManager.persist(customerExpected1);
        testEntityManager.persist(customerExpected2);
        testEntityManager.persist(customerExpected3);
        testEntityManager.flush();
    }

    @Test
    public void findAllTest() {
        List<Customer> listActual = customerRepository.findAll();
        assertEquals(List.of(customerExpected1, customerExpected2, customerExpected3), listActual);
    }

    @Test
    public void findByNameStartsWithIgnoreCaseTest() {
        List<Customer> customerListActual =
                customerRepository.findByNameStartsWithIgnoreCase("re");
        List<Customer> customerListActualEmpty =
                customerRepository.findByNameStartsWithIgnoreCase("rea");

        assertEquals(2, customerListActual.size());
        assertEquals(customerExpected2, customerListActual.get(0));
        assertEquals(customerExpected3, customerListActual.get(1));
        assertTrue(customerListActualEmpty.isEmpty());
    }

    @Test
    public void findByCodeTest() {
        Optional<Customer> customerActual = customerRepository.findByCode(customerExpected1.getCode());
        Optional<Customer> customerActualEmpty = customerRepository.findByCode("99");

        assertTrue(customerActual.isPresent());
        assertEquals(customerExpected1, customerActual.get());
        assertFalse(customerActualEmpty.isPresent());
    }

    @Test
    public void saveTest() {
        Customer customerToSave = new Customer();
        customerToSave.setCode("23465");
        customerToSave.setName("Customer to Save");
        customerToSave.setDeliveryLocation("Nagoya, Japan");
        customerToSave.setCurrency(Currency.USD.toString());

        Customer customerActual = customerRepository.save(customerToSave);
        boolean exists = customerRepository.existsByCode(customerToSave.getCode());
        List<Customer> listActual = customerRepository.findAll();

        assertEquals(customerToSave, customerActual);
        assertTrue(exists);
        assertEquals(List.of(customerExpected1, customerExpected2, customerExpected3, customerToSave), listActual);
    }

    @Test
    public void deleteTest() {
        customerRepository.delete(customerExpected1);

        boolean exists = customerRepository.existsByCode(customerExpected1.getCode());
        List<Customer> listActual = customerRepository.findAll();

        assertFalse(exists);
        assertEquals(2, listActual.size());
    }

    @Test
    public void existsByCodeTest() {
        assertTrue(customerRepository.existsByCode(customerExpected1.getCode()));
        assertFalse(customerRepository.existsByCode("99999"));
    }
}
